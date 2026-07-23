package org.example.engine.render.pass;

import org.example.engine.gl.FBO;
import org.example.engine.component.MeshRenderer;
import org.example.engine.light.Light;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class GBufferPass extends RenderPass {

    FBO gBuffer;

    public GBufferPass(int width, int height) {
        gBuffer = new FBO(width, height, 3, GL_LINEAR, true);
    }

    public GBuffer getGBuffer() {
        return new GBuffer(
                gBuffer.getColorTexture(0),
                gBuffer.getColorTexture(1),
                gBuffer.getColorTexture(2),
                gBuffer.getDepthTexture()
        );
    }

    public void render(RenderContext ctx) {
        gBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Light primaryLight = ctx.scene.getLights().isEmpty()
                ? null
                : ctx.scene.getLights().get(0);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = primaryLight;
        try {
            for (MeshRenderer renderer : ctx.scene.getComponents(MeshRenderer.class)) {
                if (renderer == null || !renderer.isEnabled()) {
                    continue;
                }

                renderer.render(ctx);
            }
        } finally {
            ctx.activeLight = previousLight;
        }
        gBuffer.unbindFrameBuffer(ctx.screenWidth, ctx.screenHeight);
    }
}
