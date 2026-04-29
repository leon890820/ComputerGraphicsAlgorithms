package org.example.engine.mesh;

import org.example.engine.material.Material;
import org.example.engine.gameobject.GameObject;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class MeshRenderer {

    SubMesh subMesh;
    Material defaultMaterial;

    FloatBuffer posBuffer;
    float[] positions;

    FloatBuffer uvBuffer;
    float[] uvs;

    FloatBuffer normalBuffer;
    float[] normals;

    FloatBuffer tangentBuffer;
    float[] tangents;

    IntBuffer vao;
    IntBuffer vbo;

    int count = 0;
    boolean initialized = false;

    private static final int VBO_POS     = 0;
    private static final int VBO_NORMAL  = 1;
    private static final int VBO_TANGENT = 2;
    private static final int VBO_UV      = 3;

    private static final int ATTRIB_POS     = 0;
    private static final int ATTRIB_NORMAL  = 1;
    private static final int ATTRIB_UV      = 2;
    private static final int ATTRIB_TANGENT = 3;

    public MeshRenderer() {
    }

    public MeshRenderer(SubMesh sub, Material mat) {
        subMesh = sub;
        defaultMaterial = mat;
    }

    public MeshRenderer setMaterial(Material mat) {
        defaultMaterial = mat;
        return this;
    }

    public Material getMaterial() {
        return defaultMaterial;
    }

    public SubMesh getSubMesh() {
        return subMesh;
    }

    public String getMaterialName() {
        return subMesh == null ? "null" : subMesh.materialName;
    }

    public void initialize() {
        if (subMesh == null) {
            System.out.println("[MeshRenderer] initialize failed: subMesh or gameObject is null");
            return;
        }

        if (initialized) {
            dispose();
        }

        positions = subMesh.getTrianglePosition();
        if (positions == null || positions.length == 0) {
            System.out.println("[MeshRenderer] initialize failed: positions is empty, subMesh = " + subMesh.materialName);
            return;
        }

        count = positions.length / 3;

        vao = MemoryUtil.memAllocInt(1);
        vbo = MemoryUtil.memAllocInt(4);

        glGenVertexArrays(vao);
        glBindVertexArray(vao.get(0));

        glGenBuffers(vbo);

        // Position
        posBuffer = allocateDirectFloatBuffer(positions.length);
        setBuffer(posBuffer, positions);
        pushVertexAttribData(ATTRIB_POS, VBO_POS, posBuffer, positions.length, 3, 0);

        // Normal
        normals = subMesh.getTriangleNormal();
        if (normals != null && normals.length > 0) {
            normalBuffer = allocateDirectFloatBuffer(normals.length);
            setBuffer(normalBuffer, normals);
            pushVertexAttribData(ATTRIB_NORMAL, VBO_NORMAL, normalBuffer, normals.length, 3, 0);
        }

        // UV
        uvs = subMesh.getTriangleUV();
        if (uvs != null && uvs.length > 0) {
            uvBuffer = allocateDirectFloatBuffer(uvs.length);
            setBuffer(uvBuffer, uvs);
            pushVertexAttribData(ATTRIB_UV, VBO_UV, uvBuffer, uvs.length, 2, 0);
        }

        // Tangent
        tangents = subMesh.getTriangleTangent();
        if (tangents != null && tangents.length > 0) {
            tangentBuffer = allocateDirectFloatBuffer(tangents.length);
            setBuffer(tangentBuffer, tangents);
            pushVertexAttribData(ATTRIB_TANGENT, VBO_TANGENT, tangentBuffer, tangents.length, 3, 0);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        initialized = true;
    }

    void pushVertexAttribData(int attribLoc, int vboIndex, FloatBuffer buffer, int size, int num, int bias) {
        int vboId = vbo.get(vboIndex);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(
                attribLoc,
                num,
                GL_FLOAT,
                false,
                0,
                bias
        );

        glEnableVertexAttribArray(attribLoc);
    }

    public void setBuffer(FloatBuffer buffer, float[] data) {
        buffer.rewind();
        buffer.put(data);
        buffer.rewind();
    }

    public void render(GameObject go) {
        render(go, defaultMaterial);
    }

    public void render(GameObject go, Material overrideMaterial) {
        if (!initialized || vao == null) return;
        if (go == null) return;

        Material useMat = overrideMaterial != null ? overrideMaterial : defaultMaterial;
        if (useMat == null) return;

        useMat.bind();
        useMat.run(go, subMesh);

        glBindVertexArray(vao.get(0));
        glDrawArrays(GL_TRIANGLES, 0, count);
        glBindVertexArray(0);

        useMat.cleanup();
        useMat.unbind();
    }

    public void debugRender(GameObject go) {
        debugRender(go, defaultMaterial);
    }

    public void debugRender(GameObject go, Material overrideMaterial) {
        if (!initialized || vao == null) return;
        if (go == null) return;

        Material useMat = overrideMaterial != null ? overrideMaterial : defaultMaterial;
        if (useMat == null) return;

        useMat.bind();
        useMat.run(go, subMesh);

        glBindVertexArray(vao.get(0));
        glDrawArrays(GL_LINES, 0, count);
        glBindVertexArray(0);

        useMat.cleanup();
        useMat.unbind();
    }

    public void dispose() {
        if (vbo != null) {
            glDeleteBuffers(vbo);
            MemoryUtil.memFree(vbo);
            vbo = null;
        }

        if (vao != null) {
            glDeleteVertexArrays(vao);
            MemoryUtil.memFree(vao);
            vao = null;
        }

        if (posBuffer != null) {
            MemoryUtil.memFree(posBuffer);
            posBuffer = null;
        }

        if (normalBuffer != null) {
            MemoryUtil.memFree(normalBuffer);
            normalBuffer = null;
        }

        if (uvBuffer != null) {
            MemoryUtil.memFree(uvBuffer);
            uvBuffer = null;
        }

        if (tangentBuffer != null) {
            MemoryUtil.memFree(tangentBuffer);
            tangentBuffer = null;
        }

        positions = null;
        normals = null;
        uvs = null;
        tangents = null;

        count = 0;
        initialized = false;
    }

    private FloatBuffer allocateDirectFloatBuffer(int size) {
        return MemoryUtil.memAllocFloat(size);
    }
}