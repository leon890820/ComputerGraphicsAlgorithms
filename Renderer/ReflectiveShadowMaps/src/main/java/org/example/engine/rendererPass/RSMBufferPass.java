package org.example.engine.rendererPass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.FBO;
import org.example.engine.light.Light;
import org.example.engine.material.RSMBufferMaterial;
import org.example.engine.render.RSMBuffer;
import org.example.engine.render.RenderContext;
import static org.lwjgl.opengl.GL33.*;

public class RSMBufferPass extends RenderPass{

    FBO RSMBuffer;
    RSMBufferMaterial rsmBufferMaterial;

    public RSMBufferPass(){
        RSMBuffer = new FBO(1024, 1024, 3, GL_LINEAR, true);
        rsmBufferMaterial = new RSMBufferMaterial("/shaders/RSMBuffer.frag","/shaders/RSMBuffer.vert");
    }
    public RSMBuffer getRSMBuffer() {
        return new RSMBuffer(
                RSMBuffer.getColorTexture(0),
                RSMBuffer.getColorTexture(1),
                RSMBuffer.getColorTexture(2),
                RSMBuffer.getDepthTexture()
        );
    }

    public void render(RenderContext ctx, Light light){
        var go = ctx.scene.getObjects();

        RSMBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            for(GameObject gameObject : go){
                gameObject.runWithMaterial(ctx, rsmBufferMaterial);
            }
        } finally {
            ctx.activeLight = previousLight;
        }
        RSMBuffer.unbindFrameBuffer(ctx.screenWidth,ctx.screenHeight);
    }
}
