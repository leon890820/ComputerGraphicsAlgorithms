package org.example.engine.render;

import org.example.engine.light.DirectionalLight;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.light.SpotLight;
import org.example.engine.render.pass.*;
import org.example.engine.scene.Scene;

public class Renderer {
    private static final int SHADOW_SIZE = 1024;

    private final ShadowPass shadowPass;
    private final PointShadowPass pointShadowPass;
    private final GBufferPass gBufferPass;
    private final ScenePass spotScenePass;
    private final ScenePass directionalScenePass;
    private final PointScenePass pointScenePass;
    private final ComputeExamplePass computeExamplePass;
    private float time;


    public Renderer(int screenWidth, int screenHeight) {
        shadowPass = new ShadowPass(SHADOW_SIZE);
        pointShadowPass = new PointShadowPass(SHADOW_SIZE);
        gBufferPass = new GBufferPass(screenWidth, screenHeight);
        spotScenePass = new ScenePass("/shaders/spotLight.frag", "/shaders/quad.vert");
        directionalScenePass = new ScenePass("/shaders/directionalLight.frag", "/shaders/quad.vert");
        pointScenePass = new PointScenePass();
        computeExamplePass = new ComputeExamplePass(32, 32);
    }

    public void render(RenderContext ctx) {
        if (ctx.scene.getRenderMode() == Scene.RenderMode.COMPUTE_EXAMPLE) {
            computeExamplePass.render(ctx, time);
            time += 0.016f;
            return;
        }

        gBufferPass.render(ctx);
        GBuffer gBuffer = gBufferPass.getGBuffer();

        for (Light light : ctx.scene.getLights()) {
            renderLight(ctx, gBuffer, light);
        }
    }

    private void renderLight(RenderContext ctx, GBuffer gBuffer, Light light) {
        if (light instanceof PointLight) {
            PointLight pointLight = (PointLight) light;
            pointShadowPass.render(ctx, pointLight);
            pointScenePass.render(
                    ctx,
                    gBuffer,
                    pointLight,
                    pointShadowPass.getDepthBuffer()
            );
            return;
        }

        if (light instanceof SpotLight) {
            SpotLight spotLight = (SpotLight) light;
            shadowPass.render(ctx, spotLight);
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
            shadowPass.render(ctx, directionalLight);
            directionalScenePass.render(
                    ctx,
                    gBuffer,
                    directionalLight,
                    shadowPass.getDepthBuffer()
            );
        }
    }
}
