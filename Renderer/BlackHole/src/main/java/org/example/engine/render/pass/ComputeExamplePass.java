package org.example.engine.render.pass;

import org.example.engine.gl.Shader;
import org.example.engine.gl.Texture;
import org.example.engine.material.TexturePreviewMaterial;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.GL_TEXTURE_FETCH_BARRIER_BIT;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

public class ComputeExamplePass extends RenderPass {
    private static final int LOCAL_SIZE_X = 16;
    private static final int LOCAL_SIZE_Y = 16;

    private final int width;
    private final int height;
    private final Shader computeShader;
    private final Texture outputTexture;
    private final TexturePreviewMaterial previewMaterial;

    public ComputeExamplePass(int width, int height) {
        this.width = width;
        this.height = height;
        this.computeShader = Shader.compute("/shaders/blackhole_example.comp");
        this.outputTexture = new Texture(width, height, GL_RGBA32F, GL_RGBA, GL_FLOAT, GL_LINEAR, false);
        this.previewMaterial = new TexturePreviewMaterial(
                "/shaders/computePreview.frag",
                "/shaders/quad.vert"
        ).setTexture(outputTexture);
    }

    public void render(RenderContext ctx, float time) {
        computeShader.bind();

        glBindImageTexture(0, outputTexture.getID(), 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
        setVector2("resolution", width, height);
        setFloat("time", time);

        int groupsX = (width + LOCAL_SIZE_X - 1) / LOCAL_SIZE_X;
        int groupsY = (height + LOCAL_SIZE_Y - 1) / LOCAL_SIZE_Y;
        glDispatchCompute(groupsX, groupsY, 1);
        glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL_TEXTURE_FETCH_BARRIER_BIT);

        computeShader.unbind();

        ctx.camera.runWithMaterial(ctx, previewMaterial);
    }

    private void setFloat(String name, float value) {
        int location = glGetUniformLocation(computeShader.getProgramId(), name);
        if (location >= 0) {
            glUniform1f(location, value);
        }
    }

    private void setVector2(String name, float x, float y) {
        int location = glGetUniformLocation(computeShader.getProgramId(), name);
        if (location >= 0) {
            glUniform2f(location, x, y);
        }
    }
}
