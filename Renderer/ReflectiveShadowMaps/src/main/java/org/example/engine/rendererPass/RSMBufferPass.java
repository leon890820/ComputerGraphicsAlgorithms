package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.material.RSMBufferMaterial;
import org.example.engine.render.RenderContext;
import static org.lwjgl.opengl.GL33.*;

public class RSMBufferPass extends RenderPass{

    FBO RSMBuffer;
    RSMBufferMaterial rsmBufferMaterial;

    public RSMBufferPass(){
        RSMBuffer = new FBO(1024, 1024, 3, GL_LINEAR, true);
        rsmBufferMaterial = new RSMBufferMaterial("/shaders/RSMBuffer.frag","/shaders/RSMBuffer.vert");
    }
    public Texture[] getBuffer() {
        return RSMBuffer.tex;
    }

    public Texture getDepth(){
        return RSMBuffer.depthTex;
    }

    @Override
    public void render(RenderContext ctx){
        var go = ctx.scene.getObjects();

        RSMBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        rsmBufferMaterial.setLight(ctx.scene.getLights().get(0));
        for(GameObject gameObject : go){
            gameObject.runWithMaterial(rsmBufferMaterial);
        }
        RSMBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}
