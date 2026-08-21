package org.example.engine.render.pass;

import org.example.engine.gameobject.Quad;
import org.example.engine.gl.Texture;
import org.example.engine.light.Light;
import org.example.engine.material.FinalSceneMaterial;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.math.Matrix4;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class FinalScenePass extends RenderPass {

    private final FinalSceneMaterial finalMaterial;
    private final Quad screenQuad;

    public FinalScenePass() {
        finalMaterial = new FinalSceneMaterial("/shaders/finalScene.frag", "/shaders/quad.vert");
        screenQuad = new Quad(finalMaterial);
    }

    public void render(RenderContext ctx, GBuffer gBuffer, Texture rawSSAOTexture, Texture ssaoTexture, Texture edgeTexture, Light light, boolean useSSAO) {
        if (ctx == null || gBuffer == null || rawSSAOTexture == null || ssaoTexture == null || edgeTexture == null) {
            return;
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        finalMaterial
                .setInputs(gBuffer, rawSSAOTexture, ssaoTexture, edgeTexture)
                .setRenderState(ctx, light)
                .setUseSSAO(useSSAO);

        MaterialRenderData data = new MaterialRenderData();
        data.modelMatrix = Matrix4.Identity();
        data.mvpMatrix = Matrix4.Identity();

        for (var renderer : screenQuad.getMeshRenderers()) {
            renderer.render(data, finalMaterial);
        }
    }
}
