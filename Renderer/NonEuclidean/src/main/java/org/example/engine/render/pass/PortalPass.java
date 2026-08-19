package org.example.engine.render.pass;

import org.example.engine.component.render.MeshRenderer;
import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.FBO;
import org.example.engine.light.Light;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.portal.Portal;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;

public class PortalPass extends RenderPass {
    private static final int PORTAL_TEXTURE_SIZE = 1024;
    private static final int MAX_RECURSION = 4;
    private static final float EXTRA_CLIP = 0.02f;

    private final Map<PortalBufferKey, FBO> buffers = new HashMap<>();
    private final Map<PortalBufferKey, Camera> cameras = new HashMap<>();
    private final SkyPass skyPass;

    public PortalPass(SkyPass skyPass) {
        this.skyPass = skyPass;
    }

    public void renderForward(RenderContext ctx) {
        if (ctx == null || ctx.scene == null || ctx.camera == null) {
            return;
        }

        renderSceneObjects(ctx, null);
        renderPortals(ctx, null, MAX_RECURSION);
    }

    public void render(RenderContext ctx) {
        if (ctx == null || ctx.scene == null || ctx.camera == null) {
            return;
        }

        renderPortals(ctx, null, MAX_RECURSION);
    }

    private void renderPortals(RenderContext ctx, Portal skipPortal, int recursionLevel) {
        if (recursionLevel <= 0) {
            return;
        }

        for (Portal portal : ctx.scene.getPortals()) {
            if (portal == null || portal == skipPortal) {
                continue;
            }

            renderPortal(ctx, portal, recursionLevel);
        }
    }

    private void renderPortal(RenderContext ctx, Portal portal, int recursionLevel) {
        Portal.Warp warp = chooseWarp(ctx.camera.transform.position, portal);
        Portal skipPortal = warp == null ? null : warp.toPortal;
        Camera portalCamera = updatePortalCamera(ctx.camera, portal, warp, recursionLevel);

        FBO fbo = getOrCreateBuffer(portal, recursionLevel);
        int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
        fbo.bindFrameBuffer();
        glViewport(0, 0, PORTAL_TEXTURE_SIZE, PORTAL_TEXTURE_SIZE);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        RenderContext portalCtx = new RenderContext(
                ctx.scene,
                portalCamera,
                PORTAL_TEXTURE_SIZE,
                PORTAL_TEXTURE_SIZE
        );

        Light previousLight = ctx.activeLight;
        Light primaryLight = ctx.scene.getLights().isEmpty()
                ? null
                : ctx.scene.getLights().get(0);
        portalCtx.activeLight = primaryLight;
        ctx.activeLight = primaryLight;

        try {
            if (skyPass != null) {
                skyPass.render(portalCtx);
            }
            renderSceneObjects(portalCtx, skipPortal);
            renderPortals(portalCtx, skipPortal, recursionLevel - 1);
        } finally {
            ctx.activeLight = previousLight;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        portal.setRenderTexture(fbo.getColorTexture(0));

        renderPortalSurface(ctx, portal);
    }

    private void renderSceneObjects(RenderContext ctx, Portal skipPortal) {
        for (MeshRenderer renderer : ctx.scene.getComponents(MeshRenderer.class)) {
            if (shouldRenderSceneObject(renderer, skipPortal)) {
                renderer.render(ctx);
            }
        }
    }

    private void renderPortalSurface(RenderContext ctx, Portal portal) {
        for (MeshRenderer renderer : portal.getMeshRenderers()) {
            if (renderer != null && renderer.isEnabled()) {
                renderer.render(ctx);
            }
        }
    }

    private FBO getOrCreateBuffer(Portal portal, int recursionLevel) {
        PortalBufferKey key = bufferKey(portal, recursionLevel);
        FBO fbo = buffers.get(key);
        if (fbo == null) {
            fbo = new FBO(PORTAL_TEXTURE_SIZE, PORTAL_TEXTURE_SIZE, 1, GL_NEAREST, true);
            buffers.put(key, fbo);
        }
        return fbo;
    }

    private Camera updatePortalCamera(Camera sourceCamera, Portal portal, Portal.Warp warp, int recursionLevel) {
        Camera portalCamera = getOrCreateCamera(portal, recursionLevel);
        portalCamera.copyProjectionAndViewFrom(sourceCamera);
        applyObliqueClip(portalCamera, portal);
        if (warp != null) {
            portalCamera.setViewMatrix(portalCamera.getViewMatrix().mult(warp.delta));
        }
        return portalCamera;
    }

    private void applyObliqueClip(Camera portalCamera, Portal sourcePortal) {
        if (portalCamera == null || sourcePortal == null) {
            return;
        }

        Vector3 normal = sourcePortal.forward();
        Vector3 cameraPosition = portalCamera.getViewMatrix().Inverse().translation();
        boolean frontDirection = Vector3.dot(cameraPosition.sub(sourcePortal.transform.position), normal) > 0.0f;
        if (frontDirection) {
            normal = normal.mult(-1.0f);
        }

        portalCamera.clipOblique(
                sourcePortal.transform.position.sub(normal.mult(EXTRA_CLIP)),
                normal.mult(-1.0f)
        );
    }

    private Camera getOrCreateCamera(Portal portal, int recursionLevel) {
        PortalBufferKey key = bufferKey(portal, recursionLevel);
        Camera camera = cameras.get(key);
        if (camera == null) {
            camera = new Camera();
            cameras.put(key, camera);
        }
        return camera;
    }

    private Portal.Warp chooseWarp(Vector3 cameraPosition, Portal portal) {
        Vector3 normal = portal.forward();
        boolean frontDirection = Vector3.dot(cameraPosition.sub(portal.transform.position), normal) > 0.0f;
        return frontDirection ? portal.front : portal.back;
    }

    private boolean shouldRenderSceneObject(MeshRenderer renderer, Portal skipPortal) {
        if (renderer == null || !renderer.isEnabled() || !renderer.isRenderedByDefaultPipeline()) {
            return false;
        }

        GameObject object = renderer.getGameObject();
        return !(object instanceof Portal) && object != skipPortal;
    }

    private PortalBufferKey bufferKey(Portal portal, int recursionLevel) {
        return new PortalBufferKey(portal, recursionLevel);
    }

    private static final class PortalBufferKey {
        private final Portal portal;
        private final int recursionLevel;

        private PortalBufferKey(Portal portal, int recursionLevel) {
            this.portal = portal;
            this.recursionLevel = recursionLevel;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PortalBufferKey)) return false;
            PortalBufferKey other = (PortalBufferKey) obj;
            return portal == other.portal && recursionLevel == other.recursionLevel;
        }

        @Override
        public int hashCode() {
            int result = System.identityHashCode(portal);
            result = 31 * result + recursionLevel;
            return result;
        }
    }
}
