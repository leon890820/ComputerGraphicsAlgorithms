package org.example.engine.render;

import org.example.engine.light.DirectionalLight;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.light.SpotLight;
import org.example.engine.render.pass.*;

import static org.lwjgl.opengl.GL33.*;

public class Renderer {
    private static final int SHADOW_SIZE = 1024;
    private static final boolean SHADOWS_ENABLED = true;

    private final ShadowPass shadowPass;
    private final PointShadowPass pointShadowPass;
    private final GBufferPass gBufferPass;
    private final ScenePass spotScenePass;
    private final ScenePass directionalScenePass;
    private final PointScenePass pointScenePass;
    private final SSAOPass ssaoPass;
    private final SSAOBlurPass ssaoBlurPass;
    private final TriangleEdgePass triangleEdgePass;
    private final FinalScenePass finalScenePass;
    private boolean ssaoEnabled = true;


    public Renderer(int screenWidth, int screenHeight) {
        shadowPass = new ShadowPass(SHADOW_SIZE);
        pointShadowPass = new PointShadowPass(SHADOW_SIZE);
        gBufferPass = new GBufferPass(screenWidth, screenHeight);
        spotScenePass = new ScenePass("/shaders/spotLight.frag", "/shaders/quad.vert");
        directionalScenePass = new ScenePass("/shaders/directionalLight.frag", "/shaders/quad.vert");
        pointScenePass = new PointScenePass();
        ssaoPass = new SSAOPass(screenWidth, screenHeight);
        ssaoBlurPass = new SSAOBlurPass(screenWidth, screenHeight);
        triangleEdgePass = new TriangleEdgePass(screenWidth, screenHeight);
        finalScenePass = new FinalScenePass();
    }

    public void toggleSSAO() {
        ssaoEnabled = !ssaoEnabled;
        System.out.println("[Renderer] SSAO " + (ssaoEnabled ? "enabled" : "disabled"));
    }

    public void render(RenderContext ctx) {
        if (ctx == null || ctx.scene == null) {
            return;
        }

        if (ctx.scene.getLights().isEmpty()) {
            clearDefaultSurface(ctx);
            ctx.scene.renderCustom(ctx);
            return;
        }

        gBufferPass.render(ctx);
        GBuffer gBuffer = gBufferPass.getGBuffer();

        if (ssaoEnabled) {
            ssaoPass.render(ctx, gBuffer);
            ssaoBlurPass.render(ctx, ssaoPass.getOcclusionTexture());
        }

        triangleEdgePass.render(ctx);
        Light light = ctx.scene.getLights().isEmpty() ? null : ctx.scene.getLights().get(0);
        finalScenePass.render(
                ctx,
                gBuffer,
                ssaoPass.getOcclusionTexture(),
                ssaoBlurPass.getBlurredTexture(),
                triangleEdgePass.getEdgeTexture(),
                light,
                ssaoEnabled
        );
    }

    private void clearDefaultSurface(RenderContext ctx) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void renderLight(RenderContext ctx, GBuffer gBuffer, Light light) {
        if (light instanceof PointLight) {
            PointLight pointLight = (PointLight) light;
            if (SHADOWS_ENABLED) {
                pointShadowPass.render(ctx, pointLight);
            }
            pointScenePass.render(
                    ctx,
                    gBuffer,
                    pointLight,
                    pointShadowPass.getDepthBuffer(),
                    ssaoEnabled ? ssaoBlurPass.getBlurredTexture() : null
            );
            return;
        }

        if (light instanceof SpotLight) {
            SpotLight spotLight = (SpotLight) light;
            if (SHADOWS_ENABLED) {
                shadowPass.render(ctx, spotLight);
            }
            spotScenePass.render(
                    ctx,
                    gBuffer,
                    spotLight,
                    shadowPass.getDepthBuffer()
            );
            return;
        }

        if (light instanceof DirectionalLight) {
            DirectionalLight directionalLight = (DirectionalLight) light;
            if (SHADOWS_ENABLED) {
                shadowPass.render(ctx, directionalLight);
            }
            directionalScenePass.render(
                    ctx,
                    gBuffer,
                    directionalLight,
                    shadowPass.getDepthBuffer(),
                    ssaoEnabled ? ssaoBlurPass.getBlurredTexture() : null
            );
        }
    }
}
