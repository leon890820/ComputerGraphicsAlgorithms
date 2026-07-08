package org.example.engine.material;

import org.example.engine.gl.Shader;
import org.example.engine.gl.Texture;
import org.example.engine.gl.TextureCube;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Set;

import static org.lwjgl.opengl.GL33.*;

public abstract class Material {

    protected static final int MAX_BONES = 100;

    public Shader shader;

    private final HashMap<String, Integer> uniformCache = new HashMap<>();
    private final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
    private FloatBuffer matrixArrayBuffer;

    public Material(String frag) {
        shader = new Shader(frag);
    }

    public Material(String frag, String vert) {
        shader = new Shader(vert, frag);
    }

    protected int getUniformLocation(String name) {
        if (uniformCache.containsKey(name)) {
            return uniformCache.get(name);
        }

        int location = glGetUniformLocation(shader.getProgramId(), name);
        uniformCache.put(name, location);

        if (location == -1) {
            System.out.println("[Material] Warning: uniform not found -> " + name);
        }

        return location;
    }

    private FloatBuffer writeMatrixToBuffer(Matrix4 m) {
        matrixBuffer.rewind();
        matrixBuffer.put(m.m);
        matrixBuffer.rewind();
        return matrixBuffer;
    }

    public void setTexture(String name, Texture tex, int unit) {
        if (tex == null || !tex.isUploaded()) {
            System.out.println("[Material] Warning: texture is null or not uploaded -> " + name);
            return;
        }

        int location = getUniformLocation(name);
        if (location < 0) return;

        tex.bind(unit);
        glUniform1i(location, unit);
    }

    public void setCubeTexture(String name, TextureCube tex, int unit) {
        if (tex == null || !tex.isUploaded()) {
            System.out.println("[Material] Warning: cube texture is null -> " + name);
            return;
        }

        int location = getUniformLocation(name);
        if (location < 0) return;

        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_CUBE_MAP, tex.getId());

        glUniform1i(location, unit);
    }

    public void unbindTexture(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void setMatrix4ToUniform(String name, Matrix4 m) {
        int location = getUniformLocation(name);
        if (location < 0) return;

        glUniformMatrix4fv(location, false, writeMatrixToBuffer(m));
    }

    public void setMatrix4ArrayToUniform(String name, Matrix4[] matrices, int maxCount) {
        int location = getUniformLocation(name);
        if (location < 0 || matrices == null || matrices.length == 0 || maxCount <= 0) return;

        int count = Math.min(matrices.length, maxCount);
        FloatBuffer buffer = ensureMatrixArrayBuffer(count * 16);

        for (int i = 0; i < count; i++) {
            Matrix4 matrix = matrices[i];
            if (matrix == null) {
                putIdentityMatrix(buffer);
            } else {
                buffer.put(matrix.m);
            }
        }

        buffer.rewind();
        glUniformMatrix4fv(location, false, buffer);
    }

    private void putIdentityMatrix(FloatBuffer buffer) {
        for (int i = 0; i < 16; i++) {
            buffer.put(i == 0 || i == 5 || i == 10 || i == 15 ? 1.0f : 0.0f);
        }
    }

    private FloatBuffer ensureMatrixArrayBuffer(int requiredFloats) {
        if (matrixArrayBuffer == null || matrixArrayBuffer.capacity() < requiredFloats) {
            if (matrixArrayBuffer != null) {
                MemoryUtil.memFree(matrixArrayBuffer);
            }
            matrixArrayBuffer = MemoryUtil.memAllocFloat(requiredFloats);
        }

        matrixArrayBuffer.clear();
        return matrixArrayBuffer;
    }

    public void setVector4ToUniform(String name, float x, float y, float z, float w) {
        int location = getUniformLocation(name);
        if (location < 0) return;

        glUniform4f(location, x, y, z, w);
    }

    public void setVector3ToUniform(String name, float x, float y, float z) {
        int location = getUniformLocation(name);
        if (location < 0) return;

        glUniform3f(location, x, y, z);
    }

    public void setVector3ToUniform(String name, Vector3 v) {
        if (v == null) return;
        setVector3ToUniform(name, v.x, v.y, v.z);
    }

    public void setVector2ToUniform(String name, float x, float y) {
        int location = getUniformLocation(name);
        if (location < 0) return;

        glUniform2f(location, x, y);
    }

    public void setFloatToUniform(String name, float x) {
        int location = getUniformLocation(name);
        if (location < 0) return;

        glUniform1f(location, x);
    }

    public void setIntToUniform(String name, int x) {
        int location = getUniformLocation(name);
        if (location < 0) return;

        glUniform1i(location, x);
    }

    protected void applySkinning(MaterialRenderData data) {
        Matrix4[] boneMatrices = data == null ? null : data.boneMatrices;
        boolean useSkinning = boneMatrices != null && boneMatrices.length > 0;
        setIntToUniform("useSkinning", useSkinning ? 1 : 0);
        if (useSkinning) {
            setMatrix4ArrayToUniform("boneMatrices[0]", boneMatrices, MAX_BONES);
        }
    }

    public void clearUniformCache() {
        uniformCache.clear();
    }

    public void bind() {
        shader.bind();
    }

    public void unbind() {
        shader.unbind();
    }

    public abstract void run(MaterialRenderData data);

    public void cleanup() {
    }

    public void collectTextures(Set<Texture> textures) {
    }

    public void dispose() {
        MemoryUtil.memFree(matrixBuffer);
        if (matrixArrayBuffer != null) {
            MemoryUtil.memFree(matrixArrayBuffer);
            matrixArrayBuffer = null;
        }
        if (shader != null) {
            shader.delete();
            shader = null;
        }
    }
}
