package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.light.Light;
import org.example.engine.material.*;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class SSRPass extends RenderPass{

    FBO SSRBuffer;
    Texture albedoTex;
    Texture normalTex;
    Texture worldPosTex;
    Texture depthTex;

    public SSRPass(int width, int height) {
        SSRBuffer = new FBO(width, height, 1, GL_LINEAR, true);
    }
    public Texture getColorTexture() {
        return SSRBuffer.getColorTexture(0);
    }

    public SSRPass setGBuffer(GBuffer gBuffer) {
        albedoTex = gBuffer.albedo;
        normalTex = gBuffer.normal;
        worldPosTex = gBuffer.position;
        depthTex = gBuffer.viewDepth;
        return this;
    }

    public void render(RenderContext ctx, Light light){
        SSRBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            for (GameObject go : ctx.scene.getObjects()) {
                var mat = go.getMaterial();
                if(mat instanceof SSRMaterial){
                    SSRMaterial ssrMat = (SSRMaterial) mat;
                    ssrMat
                            .setAlbedoTex(albedoTex)
                            .setNormalTex(normalTex)
                            .setWorldPosTex(worldPosTex)
                            .setDepthTex(depthTex);
                }
                go.run(ctx);
            }
        } finally {
            ctx.activeLight = previousLight;
        }
        SSRBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}
