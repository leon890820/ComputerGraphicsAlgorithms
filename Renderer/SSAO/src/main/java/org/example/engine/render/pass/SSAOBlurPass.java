package org.example.engine.render.pass;

import org.example.engine.gameobject.Quad;
import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.material.SSAOBlurMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class SSAOBlurPass extends RenderPass {

    private final FBO blurBuffer;
    private final SSAOBlurMaterial blurMaterial;
    private final Quad screenQuad;
    private final int width;
    private final int height;

    public SSAOBlurPass(int width, int height) {
        this.width = width;
        this.height = height;
        blurBuffer = new FBO(width, height, 1, GL_LINEAR, false);
        blurMaterial = new SSAOBlurMaterial("/shaders/ssaoBlur.frag", "/shaders/quad.vert");
        screenQuad = new Quad(blurMaterial);
    }

    public Texture getBlurredTexture() {
        return blurBuffer.getColorTexture(0);
    }

    public void render(RenderContext ctx, Texture ssaoInput) {
        if (ctx == null || ssaoInput == null) {
            return;
        }

        blurBuffer.bindFrameBuffer();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        blurMaterial
                .setInput(ssaoInput)
                .setSize(width, height);

        MaterialRenderData data = new MaterialRenderData();
        data.modelMatrix = Matrix4.Identity();
        data.mvpMatrix = Matrix4.Identity();

        for (var renderer : screenQuad.getMeshRenderers()) {
            renderer.render(data, blurMaterial);
        }

        blurBuffer.unbindFrameBuffer(ctx.screenWidth, ctx.screenHeight);
    }
}
