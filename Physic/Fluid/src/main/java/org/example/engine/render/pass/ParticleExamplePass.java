package org.example.engine.render.pass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class ParticleExamplePass extends RenderPass {
    public void render(RenderContext ctx) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.015f, 0.018f, 0.025f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        for (GameObject go : ctx.scene.getObjects()) {
            go.run(ctx);
        }
    }
}