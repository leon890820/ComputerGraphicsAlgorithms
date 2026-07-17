package org.example.engine.render;

import org.example.engine.gl.FBO;
import org.example.engine.render.pass.PostProcessPass;
import org.example.engine.render.pass.SkyBoxPass;

import static org.lwjgl.opengl.GL11.GL_LINEAR;

public class Renderer {

    private final int screenWidth;
    private final int screenHeight;
    private final FBO sceneColorBuffer;
    private final SkyBoxPass skyBoxPass;
    private final PostProcessPass postProcessPass;

    public Renderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        sceneColorBuffer = new FBO(screenWidth, screenHeight, 1, GL_LINEAR, false);
        skyBoxPass = new SkyBoxPass();
        postProcessPass = new PostProcessPass(skyBoxPass.getSkybox());
    }

    public void render(RenderContext ctx) {
        sceneColorBuffer.bindFrameBuffer();
        skyBoxPass.render(ctx);
        sceneColorBuffer.unbindFrameBuffer(screenWidth, screenHeight);

        postProcessPass.render(ctx, sceneColorBuffer.getColorTexture(0));
    }
}