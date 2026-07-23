package org.example.engine.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

public class ComputeBuffer {
    private int id;
    private int count;
    private int stride;

    public ComputeBuffer(int count, int stride) {
        this(count, stride, GL_DYNAMIC_DRAW);
    }

    public ComputeBuffer(int count, int stride, int usage) {
        if (count <= 0 || stride <= 0) {
            throw new IllegalArgumentException("ComputeBuffer count and stride must be positive");
        }

        this.count = count;
        this.stride = stride;
        id = glGenBuffers();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) count * stride, usage);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public static ComputeBuffer fromFloats(float[] data, int stride) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }

        ComputeBuffer buffer = new ComputeBuffer(data.length * Float.BYTES / stride, stride);
        FloatBuffer nativeData = MemoryUtil.memAllocFloat(data.length);
        nativeData.put(data).flip();
        buffer.setData(nativeData);
        MemoryUtil.memFree(nativeData);
        return buffer;
    }

    public static ComputeBuffer fromInts(int[] data, int stride) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }

        ComputeBuffer buffer = new ComputeBuffer(data.length * Integer.BYTES / stride, stride);
        IntBuffer nativeData = MemoryUtil.memAllocInt(data.length);
        nativeData.put(data).flip();
        buffer.setData(nativeData);
        MemoryUtil.memFree(nativeData);
        return buffer;
    }

    public void setData(ByteBuffer data) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void setData(float[] data) {
        FloatBuffer nativeData = MemoryUtil.memAllocFloat(data.length);
        nativeData.put(data).flip();
        setData(nativeData);
        MemoryUtil.memFree(nativeData);
    }

    public void setData(int[] data) {
        IntBuffer nativeData = MemoryUtil.memAllocInt(data.length);
        nativeData.put(data).flip();
        setData(nativeData);
        MemoryUtil.memFree(nativeData);
    }

    public void setData(FloatBuffer data) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void setData(IntBuffer data) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public int[] getIntData() {
        int elementCount = count * stride / Integer.BYTES;
        IntBuffer data = MemoryUtil.memAllocInt(elementCount);
        int[] result = new int[elementCount];

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        data.rewind();
        data.get(result);
        MemoryUtil.memFree(data);
        return result;
    }

    public float[] getFloatData(int elementCount) {
        int maxElementCount = count * stride / Float.BYTES;
        int readElementCount = Math.max(0, Math.min(elementCount, maxElementCount));
        FloatBuffer data = MemoryUtil.memAllocFloat(readElementCount);
        float[] result = new float[readElementCount];

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
        glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        data.rewind();
        data.get(result);
        MemoryUtil.memFree(data);
        return result;
    }

    public void bindBase(int binding) {
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, binding, id);
    }

    public void bind(int target) {
        glBindBuffer(target, id);
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public int getStride() {
        return stride;
    }

    public boolean isValid() {
        return id != 0;
    }

    public void dispose() {
        if (id != 0) {
            glDeleteBuffers(id);
            id = 0;
        }
        count = 0;
        stride = 0;
    }
}
