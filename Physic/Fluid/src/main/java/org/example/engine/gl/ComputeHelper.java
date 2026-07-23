package org.example.engine.gl;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_READ_WRITE;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.GL_TEXTURE_FETCH_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;

public final class ComputeHelper {
    public static final int DEFAULT_FILTER = GL_LINEAR;
    public static final int DEFAULT_INTERNAL_FORMAT = GL_RGBA32F;
    public static final int DEFAULT_FORMAT = GL_RGBA;
    public static final int DEFAULT_TYPE = GL_FLOAT;

    private ComputeHelper() {
    }

    public static void dispatch(ComputeShader shader, int iterationsX) {
        dispatch(shader, iterationsX, 1, 1);
    }

    public static void dispatch(ComputeShader shader, int iterationsX, int iterationsY) {
        dispatch(shader, iterationsX, iterationsY, 1);
    }

    public static void dispatch(ComputeShader shader, int iterationsX, int iterationsY, int iterationsZ) {
        int groupsX = ceilDiv(iterationsX, shader.getLocalSizeX());
        int groupsY = ceilDiv(iterationsY, shader.getLocalSizeY());
        int groupsZ = ceilDiv(iterationsZ, shader.getLocalSizeZ());
        shader.dispatchGroups(groupsX, groupsY, groupsZ);
    }

    public static void dispatch(ComputeShader shader, Texture texture) {
        dispatch(shader, texture.getWidth(), texture.getHeight(), 1);
    }

    public static void dispatch(ComputeShader shader, Texture3D texture) {
        dispatch(shader, texture.getWidth(), texture.getHeight(), texture.getDepth());
    }

    public static Texture createRenderTexture(int width, int height) {
        return createRenderTexture(width, height, DEFAULT_FILTER, DEFAULT_INTERNAL_FORMAT, DEFAULT_FORMAT, DEFAULT_TYPE);
    }

    public static Texture createRenderTexture(int width, int height, int filter, int internalFormat, int format, int type) {
        return new Texture(width, height, internalFormat, format, type, filter, false);
    }

    public static Texture ensureRenderTexture(Texture texture, int width, int height) {
        return ensureRenderTexture(texture, width, height, DEFAULT_FILTER, DEFAULT_INTERNAL_FORMAT, DEFAULT_FORMAT, DEFAULT_TYPE);
    }

    public static Texture ensureRenderTexture(
            Texture texture,
            int width,
            int height,
            int filter,
            int internalFormat,
            int format,
            int type
    ) {
        if (texture == null || !texture.isUploaded() || texture.getWidth() != width || texture.getHeight() != height) {
            release(texture);
            return createRenderTexture(width, height, filter, internalFormat, format, type);
        }

        texture.setSamplingMode(filter);
        return texture;
    }

    public static Texture3D createVolumeTexture(int size) {
        return createVolumeTexture(size, size, size);
    }

    public static Texture3D createVolumeTexture(int width, int height, int depth) {
        return new Texture3D(width, height, depth, DEFAULT_INTERNAL_FORMAT, DEFAULT_FORMAT, DEFAULT_TYPE, DEFAULT_FILTER);
    }

    public static ComputeBuffer createStructuredBuffer(int count, int stride) {
        return new ComputeBuffer(count, stride);
    }

    public static ComputeBuffer createStructuredBuffer(float[] data, int stride) {
        return ComputeBuffer.fromFloats(data, stride);
    }

    public static ComputeBuffer createStructuredBuffer(int[] data, int stride) {
        return ComputeBuffer.fromInts(data, stride);
    }

    public static void assignTexture(ComputeShader shader, Texture texture, String name, int unit) {
        shader.setTexture(name, texture, unit);
    }

    public static void assignTexture(ComputeShader shader, Texture3D texture, String name, int unit) {
        shader.setTexture(name, texture, unit);
    }

    public static void assignImage(ComputeShader shader, Texture texture, String name, int unit) {
        shader.bindImage(name, texture, unit, GL_WRITE_ONLY);
    }

    public static void assignImage(ComputeShader shader, Texture texture, String name, int unit, int access) {
        shader.bindImage(name, texture, unit, access);
    }

    public static void assignImage(ComputeShader shader, Texture3D texture, String name, int unit) {
        shader.bindImage(name, texture, unit, GL_WRITE_ONLY);
    }

    public static void assignImage(ComputeShader shader, Texture3D texture, String name, int unit, int access) {
        shader.bindImage(name, texture, unit, access);
    }

    public static void assignBuffer(ComputeShader shader, ComputeBuffer buffer, String name, int binding) {
        shader.setBuffer(name, buffer, binding);
    }

    public static void memoryBarrier() {
        glMemoryBarrier(
                GL_SHADER_IMAGE_ACCESS_BARRIER_BIT
                        | GL_SHADER_STORAGE_BARRIER_BIT
                        | GL_TEXTURE_FETCH_BARRIER_BIT
                        | GL_COMMAND_BARRIER_BIT
        );
    }

    public static void release(Texture... textures) {
        if (textures == null) return;

        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    public static void release(Texture3D... textures) {
        if (textures == null) return;

        for (Texture3D texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    public static void release(ComputeBuffer... buffers) {
        if (buffers == null) return;

        for (ComputeBuffer buffer : buffers) {
            if (buffer != null) {
                buffer.dispose();
            }
        }
    }

    public static int readOnly() {
        return GL_READ_ONLY;
    }

    public static int writeOnly() {
        return GL_WRITE_ONLY;
    }

    public static int readWrite() {
        return GL_READ_WRITE;
    }

    private static int ceilDiv(int value, int divisor) {
        return Math.max(1, (value + divisor - 1) / divisor);
    }
}
