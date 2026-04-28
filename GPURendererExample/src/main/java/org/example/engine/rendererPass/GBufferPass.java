package org.example.engine.rendererPass;

import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.gameobject.GameObject;
import org.example.engine.material.Material;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class GBufferPass extends RenderPass {

    FBO GBuffer;

    public GBufferPass() {
        GBuffer = new FBO(1024, 1024, 3, GL_LINEAR, true);
    }

    public Texture[] getBuffer() {
        return GBuffer.tex;
    }

    @Override
    public void render(RenderContext ctx) {
        GBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        var light = ctx.scene.getLights();
        for (GameObject go : ctx.scene.getObjects()) {
            Material mat = go.getMaterial();
            mat.setLight(light.get(0));
            go.run();
        }
        GBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}