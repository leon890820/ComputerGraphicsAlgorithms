package org.example.engine.render.pass;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gameobject.PRTObject;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class PRTPass extends RenderPass {

    public void render(RenderContext ctx) {
        if (ctx == null || ctx.scene == null) {
            return;
        }

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        for (GameObject object : ctx.scene.getObjects()) {
            if (object instanceof PRTObject) {
                object.run(ctx);
            }
        }
    }
}
