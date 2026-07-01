package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.material.*;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class SSRPass extends RenderPass{

    FBO SSRBuffer;
    Texture albedoTex;
    Texture normalTex;
    Texture worldPosTex;
    Texture depthTex;

    public SSRPass() {
        SSRBuffer = new FBO(1024, 1024, 1, GL_LINEAR, true);
    }
    public Texture[] getBuffer() {
        return SSRBuffer.tex;
    }

    public SSRPass setAlbedo(Texture albedo) {
        albedoTex = albedo;
        return this;
    }
    public SSRPass setNormal(Texture normal) {
        normalTex = normal;
        return this;
    }
    public SSRPass setWorldPos(Texture worldPos) {
        worldPosTex = worldPos;
        return this;
    }

    public SSRPass setDepth(Texture depth) {
        depthTex = depth;
        return this;
    }

    @Override
    public void render(RenderContext ctx){
        SSRBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        var light = ctx.scene.getLights();
        for (GameObject go : ctx.scene.getObjects()) {
            var mat = go.getMaterial();
            mat.setLight(light.get(0));
            if(mat instanceof SSRMaterial ssrMat){
                ssrMat.setAlbedoTex(albedoTex).setNormalTex(normalTex).setWorldPosTex(worldPosTex).setDepthTex(depthTex);
            }
            go.run();
        }
        SSRBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}
