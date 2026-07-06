package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.*;
import org.example.engine.light.PointLight;
import org.example.engine.material.RSMPointBufferMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.render.RSMCubeBuffer;
import org.example.engine.render.RenderContext;
import static org.lwjgl.opengl.GL33.*;

public class RSMPointBufferPass extends RenderPass{
    CubeMapFBO ShadowBuffer;
    RSMPointBufferMaterial rsmPointBufferMaterial;
    public RSMPointBufferPass(int size){
        ShadowBuffer = new CubeMapFBO(size, 3, true);
        rsmPointBufferMaterial = new RSMPointBufferMaterial("/shaders/RSMBuffer.frag", "/shaders/RSMBuffer.vert");
    }

    public void render(RenderContext ctx, PointLight light){
        rsmPointBufferMaterial.setLight(light);
        Matrix4[] shadowMatrices = light.getShadowMatrices();
        glEnable(GL_DEPTH_TEST);
        for (int face = 0; face < 6; face++) {
            ShadowBuffer.bindFace(face);
            glClear(GL_DEPTH_BUFFER_BIT);
            rsmPointBufferMaterial.setShadowMatrix(shadowMatrices[face]);
            for(GameObject go : ctx.scene.getObjects()){
                go.runWithMaterial(ctx, rsmPointBufferMaterial);
            }
        }
        ShadowBuffer.unbind(ctx.screenWidth, ctx.screenHeight);
    }

    public RSMCubeBuffer getRSMBuffer(){
        return new RSMCubeBuffer(
                ShadowBuffer.getColorTexture(0),
                ShadowBuffer.getColorTexture(1),
                ShadowBuffer.getColorTexture(2),
                ShadowBuffer.getDepthTexture()
        );
    }
}
