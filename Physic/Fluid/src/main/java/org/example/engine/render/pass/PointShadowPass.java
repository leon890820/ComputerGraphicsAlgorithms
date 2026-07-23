package org.example.engine.render.pass;

import org.example.engine.component.MeshRenderer;
import org.example.engine.gl.CubeMapFBO;
import org.example.engine.gl.TextureCube;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.material.PointShadowMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.render.RenderContext;
import static org.lwjgl.opengl.GL33.*;

public class PointShadowPass extends RenderPass {
    CubeMapFBO ShadowBuffer;
    PointShadowMaterial shadowMaterial;
    public PointShadowPass(int size){
        ShadowBuffer = new CubeMapFBO(size, 1, true);
        shadowMaterial = new PointShadowMaterial("/shaders/core/Shadow.frag", "/shaders/core/Shadow.vert");
    }

    public void render(RenderContext ctx, PointLight light) {
        Matrix4[] shadowMatrices = light.getShadowMatrices();
        glEnable(GL_DEPTH_TEST);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            for (int face = 0; face < 6; face++) {
                ShadowBuffer.bindFace(face);
                glClear(GL_DEPTH_BUFFER_BIT);
                shadowMaterial.setShadowMatrix(shadowMatrices[face]);
                for(MeshRenderer renderer : ctx.scene.getComponents(MeshRenderer.class)){
                    if (renderer == null || !renderer.isEnabled()) {
                        continue;
                    }

                    renderer.render(ctx, shadowMaterial);
                }
            }
        } finally {
            ctx.activeLight = previousLight;
        }
        ShadowBuffer.unbind(ctx.screenWidth, ctx.screenHeight);
    }
    public TextureCube[] getBuffer(){
        return ShadowBuffer.colorTex;
    }
    public TextureCube getDepthBuffer(){
        return ShadowBuffer.depthTex;
    }
}
