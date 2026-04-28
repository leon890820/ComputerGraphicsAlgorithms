package org.example.engine.rendererPass;

import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.gameobject.GameObject;
import org.example.engine.material.*;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class ShadowPass extends RenderPass {
    FBO ShadowBuffer;
    ShadowMaterial shadowMaterial;
    public ShadowPass(){
        ShadowBuffer = new FBO(1024, 1024, 1, GL_LINEAR, true);
        shadowMaterial = new ShadowMaterial("/shaders/Shadow.frag", "/shaders/Shadow.vert");
    }

    @Override
    public void render(RenderContext ctx) {
        ShadowBuffer.bindFrameBuffer();
        var light = ctx.scene.getLights();
        shadowMaterial.setLight(light.get(0));
        for(GameObject go : ctx.scene.getObjects()){
            go.runWithMaterial(shadowMaterial);
        }
        ShadowBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }

    public Texture[] getBuffer(){
        return ShadowBuffer.tex;
    }
    public Texture getDepthBuffer(){
        return ShadowBuffer.depthTex;
    }
}