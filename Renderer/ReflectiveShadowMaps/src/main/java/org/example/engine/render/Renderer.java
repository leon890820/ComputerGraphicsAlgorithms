package org.example.engine.render;

import org.example.engine.light.Light;
import org.example.engine.material.ShadingWithRSMMaterial;
import org.example.engine.rendererPass.*;

public class Renderer {
    RenderContext ctx;
    RSMBufferPass rsmBufferPass;
    GBufferPass gBufferPass;
    ShadingRSMPass shadingRSMPass;
    ScenePass scenePass;

    public Renderer(int screenWidth, int screenHeight) {
        rsmBufferPass = new RSMBufferPass();
        gBufferPass = new GBufferPass();
        shadingRSMPass = new ShadingRSMPass();
        scenePass = new ScenePass();
        var rsmBuffer = rsmBufferPass.getBuffer();
        var gBuffer = gBufferPass.getBuffer();
        var depth = rsmBufferPass.getDepth();
        shadingRSMPass.SetTextureBuffer(gBuffer[0],gBuffer[1],gBuffer[2],rsmBuffer[0],rsmBuffer[1],rsmBuffer[2], depth);
        var shadingBuffer = shadingRSMPass.getBuffer();
        scenePass.setGBuffer(shadingBuffer[0],null,null,null);
    }

    public void render(RenderContext ctx) {
        this.ctx = ctx;
        gBufferPass.render(ctx);
        rsmBufferPass.render(ctx);
        shadingRSMPass.render(ctx);
        scenePass.render(ctx);
    }


}
