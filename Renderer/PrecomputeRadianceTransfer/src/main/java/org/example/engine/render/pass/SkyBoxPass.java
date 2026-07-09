package org.example.engine.render.pass;

import org.example.engine.gl.TextureCube;
import org.example.engine.material.SkyBoxMaterial;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class SkyBoxPass extends RenderPass {

    private final SkyBoxMaterial skyBoxMaterial;

    public SkyBoxPass() {
        TextureCube skybox = new TextureCube("/textures/Skybox/church");
        skyBoxMaterial = new SkyBoxMaterial("/shaders/skybox.frag", "/shaders/skybox.vert");
        skyBoxMaterial.setSkybox(skybox);
    }

    public void render(RenderContext ctx) {
        if (ctx == null || ctx.camera == null) {
            return;
        }

        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        ctx.camera.runWithMaterial(ctx, skyBoxMaterial);

        glEnable(GL_DEPTH_TEST);
    }
}
