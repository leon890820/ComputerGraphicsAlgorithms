package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.CubeMapFBO;
import org.example.engine.gl.TextureCube;
import org.example.engine.light.PointLight;
import org.example.engine.material.PointShadowMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.render.RenderContext;
import static org.lwjgl.opengl.GL33.*;

public class PointShadowPass extends RenderPass {
    CubeMapFBO ShadowBuffer;
    PointShadowMaterial shadowMaterial;
    public PointShadowPass(){
        ShadowBuffer = new CubeMapFBO(1024, 1, true);
        shadowMaterial = new PointShadowMaterial("/shaders/Shadow.frag", "/shaders/Shadow.vert");
    }

    @Override
    public void render(RenderContext ctx) {
        var light = ctx.scene.getLights().get(0);
        shadowMaterial.setLight(light);
        Matrix4[] shadowMatrices = ((PointLight)light).getShadowMatrices();
        glEnable(GL_DEPTH_TEST);
        for (int face = 0; face < 6; face++) {
            ShadowBuffer.bindFace(face);
            glClear(GL_DEPTH_BUFFER_BIT); // ⭐ 必加
            shadowMaterial.setShadowMatrix(shadowMatrices[face]);
            for(GameObject go : ctx.scene.getObjects()){
                go.runWithMaterial(shadowMaterial);
            }
        }
        ShadowBuffer.unbind(ctx.screenWidth, ctx.screenHeight); // ⭐ 必加
    }
    public TextureCube[] getBuffer(){
        return ShadowBuffer.colorTex;
    }
    public TextureCube getDepthBuffer(){
        return ShadowBuffer.depthTex;
    }
}
