package org.example.engine.rendererPass;

import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.gl.TextureCube;
import org.example.engine.light.Light;
import org.example.engine.material.ShadingWithPointRSMMaterial;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RSMCubeBuffer;
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

    public Texture getColorTexture() {
        return ShadingRSMBuffer.getColorTexture(0);
    }

    public void SetTextureBuffer(GBuffer gBuffer, RSMCubeBuffer rsmBuffer){
        SetTextureBuffer(
                gBuffer.albedo,
                gBuffer.normal,
                gBuffer.position,
                rsmBuffer.flux,
                rsmBuffer.normal,
                rsmBuffer.position,
                rsmBuffer.depth
        );
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

    public void render(RenderContext ctx, Light light){
        ShadingRSMBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            var camera = ctx.camera;
            camera.runWithMaterial(ctx, shadingWithPointRSMMaterial);
        } finally {
            ctx.activeLight = previousLight;
        }
        ShadingRSMBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}
