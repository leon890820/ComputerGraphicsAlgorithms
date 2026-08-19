package org.example.engine.render.pass;

import org.example.engine.gl.Shader;
import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.render.RenderContext;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class SkyPass extends RenderPass {
    private final Shader shader;
    private final IntBuffer vao;
    private final IntBuffer vbo;
    private final IntBuffer ebo;
    private final FloatBuffer matrixBuffer;

    public SkyPass() {
        shader = new Shader("/shaders/sky.vert", "/shaders/sky.frag");
        vao = MemoryUtil.memAllocInt(1);
        vbo = MemoryUtil.memAllocInt(1);
        ebo = MemoryUtil.memAllocInt(1);
        matrixBuffer = MemoryUtil.memAllocFloat(16);

        float[] vertices = {
                -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                 1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                 1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
                -1.0f,  1.0f, 0.0f, 0.0f, 1.0f
        };
        int[] indices = {0, 1, 2, 0, 2, 3};

        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();

        glGenVertexArrays(vao);
        glGenBuffers(vbo);
        glGenBuffers(ebo);

        glBindVertexArray(vao.get(0));
        glBindBuffer(GL_ARRAY_BUFFER, vbo.get(0));
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo.get(0));
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        int stride = 5 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(indexBuffer);
    }

    public void render(RenderContext ctx, Texture depthTex) {
        if (ctx == null || ctx.camera == null) {
            return;
        }

        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);

        shader.bind();
        setMatrix("mvp", ctx.camera.inverseProjection());
        setMatrix("mv", ctx.camera.getViewMatrix().Inverse());

        int useDepthLocation = glGetUniformLocation(shader.getProgramId(), "useDepthTest");
        if (useDepthLocation >= 0) {
            glUniform1i(useDepthLocation, depthTex != null && depthTex.isUploaded() ? 1 : 0);
        }

        int depthLocation = glGetUniformLocation(shader.getProgramId(), "depthTex");
        if (depthLocation >= 0 && depthTex != null && depthTex.isUploaded()) {
            depthTex.bind(0);
            glUniform1i(depthLocation, 0);
        }

        glBindVertexArray(vao.get(0));
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        if (depthTex != null && depthTex.isUploaded()) {
            depthTex.unbind(0);
        }
        shader.unbind();

        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
    }

    public void render(RenderContext ctx) {
        render(ctx, null);
    }

    private void setMatrix(String name, Matrix4 matrix) {
        int location = glGetUniformLocation(shader.getProgramId(), name);
        if (location < 0 || matrix == null) {
            return;
        }

        matrixBuffer.rewind();
        matrixBuffer.put(matrix.m);
        matrixBuffer.rewind();
        glUniformMatrix4fv(location, false, matrixBuffer);
    }
}
