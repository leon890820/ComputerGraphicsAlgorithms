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
        var gbuffer = gBufferPass.getBuffer();
        var ssrbuffer = ssrPass.getBuffer();
        ssrPass.setAlbedo(gbuffer[0]).setNormal(gbuffer[1]).setWorldPos(gbuffer[2]).setDepth(gbuffer[3]);
        scenePass.setGBuffer(ssrbuffer[0]);
    }

    public void render(RenderContext ctx) {
        this.ctx = ctx;
        gBufferPass.render(ctx);
        ssrPass.render(ctx);
        scenePass.render(ctx);
    }



}
