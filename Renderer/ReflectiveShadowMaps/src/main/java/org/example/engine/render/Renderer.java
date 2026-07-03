package org.example.engine.render;

import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.rendererPass.*;

public class Renderer {
    private static final int RSM_SIZE = 1024;

    private final GBufferPass gBufferPass;
    private final ScenePass scenePass;

    private final RSMPointBufferPass rsmPointBufferPass;
    private final ShadingWithRSMPointPass shadingWithRSMPointPass;

    public Renderer(int screenWidth, int screenHeight) {
        gBufferPass = new GBufferPass(screenWidth, screenHeight);
        scenePass = new ScenePass();
        rsmPointBufferPass = new RSMPointBufferPass(RSM_SIZE);
        shadingWithRSMPointPass = new ShadingWithRSMPointPass();
    }

    public void render(RenderContext ctx) {
        PointLight light = getPrimaryPointLight(ctx);
        if (light == null) {
            return;
        }

        renderPointLight(ctx, light);
    }

    private void renderPointLight(RenderContext ctx, PointLight light){
        gBufferPass.render(ctx);
        GBuffer gBuffer = gBufferPass.getGBuffer();

        rsmPointBufferPass.render(ctx, light);
        RSMCubeBuffer rsmCubeBuffer = rsmPointBufferPass.getRSMBuffer();

        shadingWithRSMPointPass.SetTextureBuffer(gBuffer, rsmCubeBuffer);
        shadingWithRSMPointPass.render(ctx, light);

        scenePass.render(ctx, shadingWithRSMPointPass.getColorTexture(), light);
    }

    private PointLight getPrimaryPointLight(RenderContext ctx) {
        for (Light light : ctx.scene.getLights()) {
            if (light instanceof PointLight) {
                return (PointLight) light;
            }
        }
        return null;
    }
}
