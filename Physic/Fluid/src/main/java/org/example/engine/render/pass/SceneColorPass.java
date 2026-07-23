package org.example.engine.render.pass;

import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.GL_BLEND;
import static org.lwjgl.opengl.GL33.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL33.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL33.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33.GL_NEAREST;
import static org.lwjgl.opengl.GL33.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL33.GL_LINEAR;
import static org.lwjgl.opengl.GL33.glBindFramebuffer;
import static org.lwjgl.opengl.GL33.glClear;
import static org.lwjgl.opengl.GL33.glClearColor;
import static org.lwjgl.opengl.GL33.glDisable;
import static org.lwjgl.opengl.GL33.glEnable;
import static org.lwjgl.opengl.GL33.glViewport;
import static org.lwjgl.opengl.GL30.GL_DRAW_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBlitFramebuffer;

public class SceneColorPass extends RenderPass {
    private final FBO fbo;

    public SceneColorPass(int width, int height) {
        fbo = new FBO(width, height, 1, GL_LINEAR, true);
    }

    public void render(RenderContext ctx) {
        if (ctx == null || ctx.scene == null) {
            return;
        }

        fbo.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.015f, 0.018f, 0.025f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        ctx.scene.renderDefault(ctx);
        fbo.unbindFrameBuffer(ctx.screenWidth, ctx.screenHeight);

        ctx.sceneColorTexture = getColorTexture();
        ctx.sceneDepthTexture = getDepthTexture();
    }

    public void drawToScreen(RenderContext ctx) {
        if (ctx == null || ctx.camera == null) {
            return;
        }

        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo.getId());
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        glBlitFramebuffer(
                0,
                0,
                ctx.screenWidth,
                ctx.screenHeight,
                0,
                0,
                ctx.screenWidth,
                ctx.screenHeight,
                GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT,
                GL_NEAREST
        );

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    public Texture getColorTexture() {
        return fbo.getColorTexture(0);
    }

    public Texture getDepthTexture() {
        return fbo.getDepthTexture();
    }
}
