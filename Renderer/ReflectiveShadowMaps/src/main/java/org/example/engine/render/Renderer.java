package org.example.engine.render;

import org.example.engine.rendererPass.*;

public class Renderer {
    RenderContext ctx;
    GBufferPass gBufferPass;
    ScenePass scenePass;

    RSMPointBufferPass rsmPointBufferPass;
    ShadingWithRSMPointPass shadingWithRSMPointPass;

    public Renderer(int screenWidth, int screenHeight) {
        gBufferPass = new GBufferPass();
        scenePass = new ScenePass();
        rsmPointBufferPass = new RSMPointBufferPass();
        shadingWithRSMPointPass = new ShadingWithRSMPointPass();

        var rsmCubeBuffer = rsmPointBufferPass.getBuffer();
        var gBuffer = gBufferPass.getBuffer();
        var depth = rsmPointBufferPass.getDepth();
        shadingWithRSMPointPass.SetTextureBuffer(gBuffer[0],gBuffer[1],gBuffer[2],rsmCubeBuffer[0],rsmCubeBuffer[1],rsmCubeBuffer[2], depth);
        var shadingBuffer = shadingWithRSMPointPass.getBuffer();
        scenePass.setGBuffer(shadingBuffer[0]);
    }

    public void render(RenderContext ctx) {
        this.ctx = ctx;
        renderPointLight();
    }

    public  void renderPointLight(){
        gBufferPass.render(ctx);
        rsmPointBufferPass.render(ctx);
        shadingWithRSMPointPass.render(ctx);
        scenePass.render(ctx);
    }
}
