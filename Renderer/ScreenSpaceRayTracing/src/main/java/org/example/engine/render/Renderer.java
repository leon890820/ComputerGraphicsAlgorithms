package org.example.engine.render;

import org.example.engine.light.Light;
import org.example.engine.rendererPass.*;

public class Renderer {
    private final GBufferPass gBufferPass;
    private final SSRPass ssrPass;
    private final ScenePass scenePass;


    public Renderer(int screenWidth, int screenHeight) {
        gBufferPass = new GBufferPass(screenWidth, screenHeight);
        ssrPass = new SSRPass(screenWidth, screenHeight);
        scenePass = new SpotScenePass();
    }

    public void render(RenderContext ctx) {
        Light light = getPrimaryLight(ctx);

        gBufferPass.render(ctx, light);
        GBuffer gBuffer = gBufferPass.getGBuffer();

        ssrPass.setGBuffer(gBuffer);
        ssrPass.render(ctx, light);

        scenePass.setAlbedo(ssrPass.getColorTexture());
        scenePass.render(ctx);
    }

    private Light getPrimaryLight(RenderContext ctx) {
        for (Light light : ctx.scene.getLights()) {
            return light;
        }
        return null;
    }

}
