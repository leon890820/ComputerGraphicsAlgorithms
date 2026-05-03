package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.*;
import org.example.engine.light.PointLight;
import org.example.engine.material.RSMPointBufferMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.render.RenderContext;
import static org.lwjgl.opengl.GL33.*;

public class RSMPointBufferPass extends RenderPass{
    CubeMapFBO ShadowBuffer;
    RSMPointBufferMaterial rsmPointBufferMaterial;
    public RSMPointBufferPass(){
        ShadowBuffer = new CubeMapFBO(1024, 3, true);
        rsmPointBufferMaterial = new RSMPointBufferMaterial("/shaders/RSMBuffer.frag", "/shaders/RSMBuffer.vert");
    }

    @Override
    public void render(RenderContext ctx){
        var light = ctx.scene.getLights().get(0);
        rsmPointBufferMaterial.setLight(light);
        Matrix4[] shadowMatrices = ((PointLight)light).getShadowMatrices();
        glEnable(GL_DEPTH_TEST);
        for (int face = 0; face < 6; face++) {
            ShadowBuffer.bindFace(face);
            glClear(GL_DEPTH_BUFFER_BIT); // ⭐ 必加
            rsmPointBufferMaterial.setShadowMatrix(shadowMatrices[face]);
            for(GameObject go : ctx.scene.getObjects()){
                go.runWithMaterial(rsmPointBufferMaterial);
            }
        }
        ShadowBuffer.unbind(ctx.screenWidth, ctx.screenHeight); // ⭐ 必加
    }

    public TextureCube[] getBuffer(){
        return ShadowBuffer.colorTex;
    }
    public TextureCube getDepth(){
        return ShadowBuffer.depthTex;
    }
}
