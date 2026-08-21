package org.example.engine.render.pass;

import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.gameobject.Quad;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.material.SSAOMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class SSAOPass extends RenderPass {

    private final SSAOMaterial ssaoMaterial;
    private final Quad screenQuad;
    private final FBO ssaoBuffer;

    public SSAOPass(int width, int height) {
        ssaoMaterial = new SSAOMaterial("/shaders/ssaoDebug.frag", "/shaders/quad.vert");
        screenQuad = new Quad(ssaoMaterial);
        ssaoBuffer = new FBO(width, height, 1, GL_LINEAR, false);
    }

    public Texture getOcclusionTexture() {
        return ssaoBuffer.getColorTexture(0);
    }

    public void render(RenderContext ctx, GBuffer gBuffer) {
        if (ctx == null || gBuffer == null) {
            return;
        }

        ssaoBuffer.bindFrameBuffer();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        ssaoMaterial
                .setGBuffer(gBuffer)
                .setRenderContext(ctx);

        MaterialRenderData data = new MaterialRenderData();
        data.modelMatrix = Matrix4.Identity();
        data.mvpMatrix = Matrix4.Identity();

        for (var renderer : screenQuad.getMeshRenderers()) {
            renderer.render(data, ssaoMaterial);
        }

        ssaoBuffer.unbindFrameBuffer(ctx.screenWidth, ctx.screenHeight);
    }
}
