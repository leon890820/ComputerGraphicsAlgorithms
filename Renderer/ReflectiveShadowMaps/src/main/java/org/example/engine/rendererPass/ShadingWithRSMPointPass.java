package org.example.engine.rendererPass;

import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.gl.TextureCube;
import org.example.engine.material.ShadingWithPointRSMMaterial;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class ShadingWithRSMPointPass extends RenderPass{

    FBO ShadingRSMBuffer;
    ShadingWithPointRSMMaterial shadingWithPointRSMMaterial;

    public ShadingWithRSMPointPass(){
        ShadingRSMBuffer = new FBO(1024, 1024, 3, GL_LINEAR, true);
        shadingWithPointRSMMaterial = new ShadingWithPointRSMMaterial("/shaders/ShadingWithPointRSM.frag","/shaders/ShadingWithRSM.vert");
    }
    public Texture[] getBuffer() {
        return ShadingRSMBuffer.tex;
    }

    public void SetTextureBuffer(Texture Albedo, Texture Normal, Texture Position, TextureCube RSMFlux, TextureCube RSMNormal, TextureCube RSMPosition, TextureCube depth){
        shadingWithPointRSMMaterial.setAlbedoTexture(Albedo)
                .setNormalTexture(Normal)
                .setPositionTexture(Position)
                .setRSMFluxTexture(RSMFlux)
                .setRSMNormalTexture(RSMNormal)
                .setRSMPositionTexture(RSMPosition)
                .setRSMDepthTexture(depth);

    }

    @Override
    public void render(RenderContext ctx){
        ShadingRSMBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        shadingWithPointRSMMaterial.setLight(ctx.scene.getLights().get(0));
        var camera = ctx.scene.getCamera();
        camera.runWithMaterial(shadingWithPointRSMMaterial);
        ShadingRSMBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}
