package org.example.engine.render.pass;

import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.gameobject.GameObject;
import org.example.engine.light.Light;
import org.example.engine.material.*;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class ShadowPass extends RenderPass {
    FBO ShadowBuffer;
    ShadowMaterial shadowMaterial;
    public ShadowPass(int size){
        ShadowBuffer = new FBO(size, size, 1, GL_LINEAR, true);
        shadowMaterial = new ShadowMaterial("/shaders/Shadow.frag", "/shaders/Shadow.vert");
    }

    public void render(RenderContext ctx, Light light) {
        ShadowBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shadowMaterial.setLight(light);
        for(GameObject go : ctx.scene.getObjects()){
            go.runWithMaterial(ctx, shadowMaterial);
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
