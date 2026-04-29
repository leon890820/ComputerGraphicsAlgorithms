package org.example.engine.gl;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL43.*;

public class SSBO {

    private int ssboId;
    private int bindingPoint;

    private int elementCount = 0;
    private boolean initialized = false;

    public SSBO(int bindingPoint, float[] data) {
        this.bindingPoint = bindingPoint;

        ssboId = glGenBuffers();

        uploadData(data, GL_STATIC_DRAW);
        bindBase();
    }

    public void bind() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssboId);
    }

    public void unbind() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void bindBase() {
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, bindingPoint, ssboId);
    }

    public void uploadData(float[] data, int usage) {
        if (ssboId == 0) return;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data).flip();

        bind();
        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, usage);
        unbind();

        elementCount = data.length;
        initialized = true;
    }

    public void updateData(float[] data) {
        if (ssboId == 0 || !initialized) return;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data).flip();

        bind();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, buffer);
        unbind();

        elementCount = data.length;
    }

    public int getBindingPoint() {
        return bindingPoint;
    }

    public int getBufferId() {
        return ssboId;
    }

    public int getElementCount() {
        return elementCount;
    }

    public void dispose() {
        if (ssboId != 0) {
            glDeleteBuffers(ssboId);
            ssboId = 0;
        }
        initialized = false;
        elementCount = 0;
    }
}