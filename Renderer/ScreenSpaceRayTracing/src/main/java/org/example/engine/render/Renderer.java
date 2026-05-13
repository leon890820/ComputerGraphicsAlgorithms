package org.example.engine.render;

import org.example.engine.light.Light;
import org.example.engine.rendererPass.*;

public class Renderer {
    RenderContext ctx;
    public GBufferPass gBufferPass;
    public SSRPass ssrPass;
    public ScenePass scenePass;


    public Renderer(int screenWidth, int screenHeight) {
        gBufferPass = new GBufferPass();
        ssrPass = new SSRPass();
        scenePass = new SpotScenePass();
        var buffer = gBufferPass.getBuffer();
        scenePass.setGBuffer(buffer[0], buffer[1], buffer[2], null);
    }

    public void render(RenderContext ctx) {
        this.ctx = ctx;
        gBufferPass.render(ctx);
        //ssrPass.render(ctx);
        scenePass.render(ctx);
    }



}
