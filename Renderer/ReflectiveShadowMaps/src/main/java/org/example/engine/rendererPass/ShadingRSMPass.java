package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.material.ShadingWithRSMMaterial;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;


public class ShadingRSMPass extends RenderPass{

    FBO ShadingRSMBuffer;
    ShadingWithRSMMaterial shadingRSMBufferMaterial;

    public ShadingRSMPass(){
        ShadingRSMBuffer = new FBO(1024, 1024, 3, GL_LINEAR, true);
        shadingRSMBufferMaterial = new ShadingWithRSMMaterial("/shaders/ShadingWithRSM.frag","/shaders/ShadingWithRSM.vert");
    }
    public Texture[] getBuffer() {
        return ShadingRSMBuffer.tex;
    }

    public void SetTextureBuffer(Texture Albedo, Texture Normal, Texture Position, Texture RSMFlux, Texture RSMNormal, Texture RSMPosition, Texture depth){
        shadingRSMBufferMaterial.setAlbedoTexture(Albedo)
                .setNormalTexture(Normal)
                .setPositionTexture(Position)
                .setRSMFluxTexture(RSMFlux)
                .setRSMNormalTexture(RSMNormal)
                .setRSMPositionTexture(RSMPosition)
                .setRSMDepthTexture(depth);

    }

    @Override
    public void render(RenderContext ctx){
        var go = ctx.scene.getObjects();

        ShadingRSMBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shadingRSMBufferMaterial.setLight(ctx.scene.getLights().get(0));
        var camera = ctx.scene.getCamera();
        camera.runWithMaterial(shadingRSMBufferMaterial);
        ShadingRSMBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}
