package org.example.engine.gl;

import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL43.*;

public class ComputeShader {
    private final int programId;
    private final int[] localSize = new int[3];
    private final HashMap<String, Integer> uniformCache = new HashMap<>();
    private final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);

    public ComputeShader(String computePath) {
        String computeSrc = loadResource(computePath);
        int shader = compile(GL_COMPUTE_SHADER, computeSrc);

        programId = glCreateProgram();
        glAttachShader(programId, shader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Compute shader link failed:\n" + glGetProgramInfoLog(programId));
        }

        glDeleteShader(shader);
        readLocalSize();
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int getProgramId() {
        return programId;
    }

    public int getLocalSizeX() {
        return localSize[0];
    }

    public int getLocalSizeY() {
        return localSize[1];
    }

    public int getLocalSizeZ() {
        return localSize[2];
    }

    public void bindImage(String name, Texture texture, int unit, int access) {
        bindImage(name, texture, unit, access, GL_RGBA32F);
    }

    public void bindImage(String name, Texture texture, int unit, int access, int format) {
        if (texture == null || !texture.isUploaded()) {
            System.out.println("[ComputeShader] Warning: image texture is null or not uploaded -> " + name);
            return;
        }

        glBindImageTexture(unit, texture.getID(), 0, false, 0, access, format);
        setInt(name, unit);
    }

    public void bindImage(String name, Texture3D texture, int unit, int access) {
        bindImage(name, texture, unit, access, GL_RGBA32F);
    }

    public void bindImage(String name, Texture3D texture, int unit, int access, int format) {
        if (texture == null || !texture.isUploaded()) {
            System.out.println("[ComputeShader] Warning: 3D image texture is null or not uploaded -> " + name);
            return;
        }

        glBindImageTexture(unit, texture.getID(), 0, true, 0, access, format);
        setInt(name, unit);
    }

    public void setTexture(String name, Texture texture, int unit) {
        if (texture == null || !texture.isUploaded()) {
            System.out.println("[ComputeShader] Warning: texture is null or not uploaded -> " + name);
            return;
        }

        texture.bind(unit);
        setInt(name, unit);
    }

    public void setTexture(String name, Texture3D texture, int unit) {
        if (texture == null || !texture.isUploaded()) {
            System.out.println("[ComputeShader] Warning: 3D texture is null or not uploaded -> " + name);
            return;
        }

        texture.bind(unit);
        setInt(name, unit);
    }

    public void setBuffer(String name, ComputeBuffer buffer, int binding) {
        if (buffer == null || !buffer.isValid()) {
            System.out.println("[ComputeShader] Warning: buffer is null or disposed -> " + name);
            return;
        }

        buffer.bindBase(binding);
        setInt(name, binding);
    }

    public void setInt(String name, int value) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform1i(location, value);
        }
    }

    public void setBool(String name, boolean value) {
        setInt(name, value ? 1 : 0);
    }

    public void setFloat(String name, float value) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform1f(location, value);
        }
    }

    public void setVector2(String name, float x, float y) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform2f(location, x, y);
        }
    }

    public void setVector3(String name, Vector3 value) {
        if (value != null) {
            setVector3(name, value.x, value.y, value.z);
        }
    }

    public void setVector3(String name, float x, float y, float z) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform3f(location, x, y, z);
        }
    }

    public void setVector4(String name, float x, float y, float z, float w) {
        int location = getUniformLocation(name);
        if (location >= 0) {
            glUniform4f(location, x, y, z, w);
        }
    }

    public void setMatrix4(String name, Matrix4 matrix) {
        if (matrix == null) return;

        int location = getUniformLocation(name);
        if (location < 0) return;

        matrixBuffer.rewind();
        matrixBuffer.put(matrix.m);
        matrixBuffer.rewind();
        glUniformMatrix4fv(location, false, matrixBuffer);
    }

    public void dispatchGroups(int groupsX, int groupsY, int groupsZ) {
        glDispatchCompute(Math.max(1, groupsX), Math.max(1, groupsY), Math.max(1, groupsZ));
    }

    public void dispose() {
        MemoryUtil.memFree(matrixBuffer);
        glDeleteProgram(programId);
    }

    private void readLocalSize() {
        IntBuffer sizes = MemoryUtil.memAllocInt(3);
        glGetProgramiv(programId, GL_COMPUTE_WORK_GROUP_SIZE, sizes);
        localSize[0] = Math.max(1, sizes.get(0));
        localSize[1] = Math.max(1, sizes.get(1));
        localSize[2] = Math.max(1, sizes.get(2));
        MemoryUtil.memFree(sizes);
    }

    private int getUniformLocation(String name) {
        if (uniformCache.containsKey(name)) {
            return uniformCache.get(name);
        }

        int location = glGetUniformLocation(programId, name);
        uniformCache.put(name, location);

        if (location == -1) {
            System.out.println("[ComputeShader] Warning: uniform not found -> " + name);
        }

        return location;
    }

    private static int compile(int type, String src) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Compute shader compile failed:\n" + glGetShaderInfoLog(shader));
        }

        return shader;
    }

    private static String loadResource(String path) {
        InputStream is = ComputeShader.class.getResourceAsStream(path);

        if (is == null) {
            throw new RuntimeException("Compute shader not found: " + path);
        }

        return new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}