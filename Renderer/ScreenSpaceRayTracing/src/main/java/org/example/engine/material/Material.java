package org.example.engine.material;

import org.example.engine.gl.Shader;
import org.example.engine.gl.Texture;
import org.example.engine.gl.TextureCube;
import org.example.engine.light.Light;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.SubMesh;
import org.example.engine.gameobject.GameObject;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL33.*;

public abstract class Material {

    public Shader shader;

    private final HashMap<String, Integer> uniformCache = new HashMap<>();
    private final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);

    Light lightSource;

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

    public void clearUniformCache() {
        uniformCache.clear();
    }

    public void bind() {
        shader.bind();
    }

    public void unbind() {
        shader.unbind();
    }

    public Material setLight(Light l) {
        lightSource = l;
        return this;
    }

    public abstract void run(GameObject go, SubMesh subMesh);

    public void cleanup() {
    }

    public void dispose() {
        MemoryUtil.memFree(matrixBuffer);
        if (shader != null) {
            shader.delete();
            shader = null;
        }
    }
}