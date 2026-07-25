package org.example.engine.component.render;

import org.example.engine.component.core.Component;
import org.example.engine.gl.FBO;
import org.example.engine.gameobject.Quad;
import org.example.engine.material.RayTracingMaterial;
import org.example.engine.material.TexturePreviewMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.raytracing.RayTracingMeshInstance;
import org.example.engine.raytracing.RayTracingSceneBuilder;
import org.example.engine.raytracing.RayTracingSceneBuffers;
import org.example.engine.render.RenderContext;

import java.util.List;

import static org.lwjgl.opengl.GL33.*;

public class RayTracingDisplay extends Component {
    private static final int MAX_BOUNCES = 5;
    private static final float RENDER_SCALE = 1.0f;

    private final int screenWidth;
    private final int screenHeight;
    private final int renderWidth;
    private final int renderHeight;
    private final String meshPath;
    private final List<RayTracingMeshInstance> meshInstances;
    private final RayTracingMaterial rayTracingMaterial;
    private final TexturePreviewMaterial previewMaterial;
    private final RayTracingSceneBuilder sceneBuilder;

    private RayTracingSceneBuffers sceneBuffers;
    private FBO[] accumulationBuffers;
    private Quad screenQuad;
    private int currentBufferIndex;
    private int frameIndex;
    private Vector3 lastCameraPosition;
    private Vector3 lastCameraEuler;

    public RayTracingDisplay(int screenWidth, int screenHeight, String meshPath) {
        this(screenWidth, screenHeight, meshPath, null);
    }

    public RayTracingDisplay(int screenWidth, int screenHeight, List<RayTracingMeshInstance> meshInstances) {
        this(screenWidth, screenHeight, null, meshInstances);
    }

    private RayTracingDisplay(int screenWidth, int screenHeight, String meshPath, List<RayTracingMeshInstance> meshInstances) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        renderWidth = Math.max(1, Math.round(screenWidth * RENDER_SCALE));
        renderHeight = Math.max(1, Math.round(screenHeight * RENDER_SCALE));
        this.meshPath = meshPath;
        this.meshInstances = meshInstances;
        sceneBuilder = new RayTracingSceneBuilder();
        rayTracingMaterial = new RayTracingMaterial("/shaders/raytracing.frag", "/shaders/raytracing.vert")
                .setResolution(renderWidth, renderHeight);
        previewMaterial = new TexturePreviewMaterial("/shaders/screen_texture.frag", "/shaders/raytracing.vert");
    }

    @Override
    public void start() {
        sceneBuffers = meshInstances == null
                ? sceneBuilder.buildDragonCornellScene(meshPath)
                : sceneBuilder.buildStaticScene(meshInstances);
        rayTracingMaterial
                .setSceneBuffers(sceneBuffers)
                .setMaxBounces(MAX_BOUNCES);
        accumulationBuffers = new FBO[]{
                new FBO(renderWidth, renderHeight, 1, GL_LINEAR, false),
                new FBO(renderWidth, renderHeight, 1, GL_LINEAR, false)
        };
        screenQuad = new Quad(rayTracingMaterial);
    }

    @Override
    public void render(RenderContext ctx) {
        if (ctx == null || ctx.camera == null || sceneBuffers == null || accumulationBuffers == null || screenQuad == null) {
            return;
        }

        resetAccumulationIfCameraMoved(ctx);

        int writeIndex = currentBufferIndex;
        int readIndex = 1 - currentBufferIndex;

        FBO writeBuffer = accumulationBuffers[writeIndex];
        FBO readBuffer = accumulationBuffers[readIndex];

        writeBuffer.bindFrameBuffer();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        rayTracingMaterial
                .setLastFrame(readBuffer.getColorTexture(0))
                .setCamera(ctx.camera.transform.position, ctx.camera.inverseProjection(), ctx.camera.localToWorld())
                .setAccumulationBias(frameIndex)
                .setDarkBackground(true);

        for (MeshRenderer renderer : screenQuad.getMeshRenderers()) {
            renderer.render(ctx, rayTracingMaterial);
        }
        writeBuffer.unbindFrameBuffer(ctx.screenWidth, ctx.screenHeight);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        previewMaterial.setTexture(writeBuffer.getColorTexture(0));
        for (MeshRenderer renderer : screenQuad.getMeshRenderers()) {
            renderer.render(ctx, previewMaterial);
        }

        currentBufferIndex = readIndex;
        frameIndex++;
    }

    @Override
    public boolean isRenderedByDefaultPipeline() {
        return false;
    }

    @Override
    public void dispose() {
        if (sceneBuffers != null) {
            sceneBuffers.dispose();
            sceneBuffers = null;
        }
        if (accumulationBuffers != null) {
            for (FBO buffer : accumulationBuffers) {
                if (buffer != null) {
                    buffer.dispose();
                }
            }
            accumulationBuffers = null;
        }
        if (screenQuad != null) {
            screenQuad.dispose();
            screenQuad = null;
        }
        rayTracingMaterial.dispose();
        previewMaterial.dispose();
    }

    private void resetAccumulationIfCameraMoved(RenderContext ctx) {
        Vector3 cameraPosition = ctx.camera.transform.position;
        Vector3 cameraEuler = ctx.camera.transform.eular;
        if (lastCameraPosition == null || lastCameraEuler == null
                || !near(lastCameraPosition, cameraPosition)
                || !near(lastCameraEuler, cameraEuler)) {
            frameIndex = 0;
            lastCameraPosition = cameraPosition.copy();
            lastCameraEuler = cameraEuler.copy();
        }
    }

    private boolean near(Vector3 a, Vector3 b) {
        return Math.abs(a.x - b.x) < 0.0001f
                && Math.abs(a.y - b.y) < 0.0001f
                && Math.abs(a.z - b.z) < 0.0001f;
    }
}
