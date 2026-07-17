package org.example.engine.render.pass;

import org.example.engine.gl.Texture;
import org.example.engine.gl.TextureCube;
import org.example.engine.material.PostProcessMaterial;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class PostProcessPass extends RenderPass {

    private final PostProcessMaterial material;

    public PostProcessPass(TextureCube skybox) {
        material = new PostProcessMaterial("/shaders/postProcess.frag", "/shaders/quad.vert");
        material.setSkybox(skybox);
    }

    public void render(RenderContext ctx, Texture sourceTexture) {
        if (ctx == null || ctx.camera == null || sourceTexture == null) {
            return;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        material.setSourceTexture(sourceTexture);
        ctx.camera.runWithMaterial(ctx, material);

        glEnable(GL_DEPTH_TEST);
    }
}