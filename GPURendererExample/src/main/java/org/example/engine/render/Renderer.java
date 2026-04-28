package org.example.engine.render;

import org.example.engine.light.Light;
import org.example.engine.rendererPass.*;

public class Renderer {
    RenderContext ctx;
    public ShadowPass shadowPass;
    public PointShadowPass pointShadowPass;
    public GBufferPass gBufferPass;
    public SpotScenePass spotScenePass;
    public DirectionalScenePass directionalScenePass;
    public PointScenePass pointScenePass;


    public Renderer(int screenWidth, int screenHeight) {
        shadowPass = new ShadowPass();
        pointShadowPass = new PointShadowPass();
        gBufferPass = new GBufferPass();
        spotScenePass = new SpotScenePass();
        directionalScenePass = new DirectionalScenePass();
        pointScenePass = new PointScenePass();
    }

    public void render(RenderContext ctx) {
        this.ctx = ctx;
        renderShadowPasses();
        gBufferPass.render(ctx);
        renderScenePasses();
    }

    private void renderShadowPasses() {
        for (Light light : ctx.scene.getLights()) {
            light.renderShadow(ctx, this);
        }
    }

    private void renderScenePasses() {
        for (Light light : ctx.scene.getLights()) {
            light.renderLighting(ctx, this);
        }
    }

}
