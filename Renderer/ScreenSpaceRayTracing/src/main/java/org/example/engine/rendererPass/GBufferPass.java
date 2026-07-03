package org.example.engine.rendererPass;

import org.example.engine.gl.FBO;
import org.example.engine.gameobject.GameObject;
import org.example.engine.light.Light;
import org.example.engine.material.*;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class GBufferPass extends RenderPass {

    FBO gBuffer;
    GBufferMaterial gBufferMaterial;


    public GBufferPass(int width, int height) {
        gBuffer = new FBO(width, height, 4, GL_NEAREST, true);
        gBufferMaterial = new GBufferMaterial("/shaders/GBuffer.frag", "/shaders/GBuffer.vert");
    }

    public GBuffer getGBuffer() {
        return new GBuffer(
                gBuffer.getColorTexture(0),
                gBuffer.getColorTexture(1),
                gBuffer.getColorTexture(2),
                gBuffer.getColorTexture(3),
                gBuffer.getDepthTexture()
        );
    }

    public void render(RenderContext ctx, Light light) {
        gBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        for (GameObject go : ctx.scene.getObjects()) {
            if (light != null) {
                gBufferMaterial.setLight(light);
            }
            go.runWithMaterial(ctx, gBufferMaterial);
        }
        gBuffer.unbindFrameBuffer(ctx.screenWidth, ctx.screenHeight);
    }
}
