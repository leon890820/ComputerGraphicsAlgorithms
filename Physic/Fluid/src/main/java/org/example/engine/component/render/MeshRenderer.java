package org.example.engine.component.render;

import org.example.engine.component.core.Component;
import org.example.engine.material.Material;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.mesh.SubMesh;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class MeshRenderer extends Component {

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

    IntBuffer boneIdBuffer;
    int[] boneIds;

    FloatBuffer boneWeightBuffer;
    float[] boneWeights;

    IntBuffer indexBuffer;
    int[] indices;

    IntBuffer vao;
    IntBuffer vbo;
    IntBuffer ebo;

    int count = 0;
    boolean initialized = false;
    private boolean renderedByDefaultPipeline = true;

    private static final int VBO_POS     = 0;
    private static final int VBO_NORMAL  = 1;
    private static final int VBO_TANGENT = 2;
    private static final int VBO_UV      = 3;
    private static final int VBO_BONE_ID = 4;
    private static final int VBO_BONE_WEIGHT = 5;

    private static final int ATTRIB_POS     = 0;
    private static final int ATTRIB_NORMAL  = 1;
    private static final int ATTRIB_UV      = 2;
    private static final int ATTRIB_TANGENT = 3;
    private static final int ATTRIB_BONE_ID = 4;
    private static final int ATTRIB_BONE_WEIGHT = 5;

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

    public MeshRenderer setRenderedByDefaultPipeline(boolean renderedByDefaultPipeline) {
        this.renderedByDefaultPipeline = renderedByDefaultPipeline;
        return this;
    }

    public void initialize() {
        if (subMesh == null) {
            System.out.println("[MeshRenderer] initialize failed: subMesh is null");
            return;
        }

        if (initialized) {
            dispose();
        }

        positions = subMesh.positions;
        if (positions == null || positions.length == 0) {
            System.out.println("[MeshRenderer] initialize failed: positions is empty, subMesh = " + subMesh.materialName);
            return;
        }

        indices = subMesh.indices;
        if (indices == null || indices.length == 0) {
            System.out.println("[MeshRenderer] initialize failed: indices is empty, subMesh = " + subMesh.materialName);
            return;
        }

        count = indices.length;

        vao = MemoryUtil.memAllocInt(1);
        vbo = MemoryUtil.memAllocInt(6);
        ebo = MemoryUtil.memAllocInt(1);

        glGenVertexArrays(vao);
        glBindVertexArray(vao.get(0));

        glGenBuffers(vbo);
        glGenBuffers(ebo);

        // Position
        posBuffer = allocateDirectFloatBuffer(positions.length);
        setBuffer(posBuffer, positions);
        pushVertexAttribData(ATTRIB_POS, VBO_POS, posBuffer, positions.length, 3, 0);

        // Normal
        normals = subMesh.normals;
        if (normals != null && normals.length > 0) {
            normalBuffer = allocateDirectFloatBuffer(normals.length);
            setBuffer(normalBuffer, normals);
            pushVertexAttribData(ATTRIB_NORMAL, VBO_NORMAL, normalBuffer, normals.length, 3, 0);
        }

        // UV
        uvs = subMesh.uvs;
        if (uvs != null && uvs.length > 0) {
            uvBuffer = allocateDirectFloatBuffer(uvs.length);
            setBuffer(uvBuffer, uvs);
            pushVertexAttribData(ATTRIB_UV, VBO_UV, uvBuffer, uvs.length, 2, 0);
        }

        // Tangent
        tangents = subMesh.tangents;
        if (tangents != null && tangents.length > 0) {
            tangentBuffer = allocateDirectFloatBuffer(tangents.length);
            setBuffer(tangentBuffer, tangents);
            pushVertexAttribData(ATTRIB_TANGENT, VBO_TANGENT, tangentBuffer, tangents.length, 3, 0);
        }

        if (subMesh.hasSkinWeights()) {
            boneIds = subMesh.boneIds;
            boneWeights = subMesh.boneWeights;

            int vertexCount = positions.length / 3;
            if (boneIds.length == vertexCount * 4 && boneWeights.length == vertexCount * 4) {
                boneIdBuffer = MemoryUtil.memAllocInt(boneIds.length);
                setIntBuffer(boneIdBuffer, boneIds);
                pushVertexAttribIntData(ATTRIB_BONE_ID, VBO_BONE_ID, boneIdBuffer, 4);

                boneWeightBuffer = allocateDirectFloatBuffer(boneWeights.length);
                setBuffer(boneWeightBuffer, boneWeights);
                pushVertexAttribData(ATTRIB_BONE_WEIGHT, VBO_BONE_WEIGHT, boneWeightBuffer, boneWeights.length, 4, 0);
            }
        }

        indexBuffer = MemoryUtil.memAllocInt(indices.length);
        setIntBuffer(indexBuffer, indices);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo.get(0));
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

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

    void pushVertexAttribIntData(int attribLoc, int vboIndex, IntBuffer buffer, int num) {
        int vboId = vbo.get(vboIndex);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glVertexAttribIPointer(
                attribLoc,
                num,
                GL_INT,
                0,
                0
        );

        glEnableVertexAttribArray(attribLoc);
    }

    public void setBuffer(FloatBuffer buffer, float[] data) {
        buffer.rewind();
        buffer.put(data);
        buffer.rewind();
    }

    public void setIntBuffer(IntBuffer buffer, int[] data) {
        buffer.rewind();
        buffer.put(data);
        buffer.rewind();
    }

    public void render(MaterialRenderData data) {
        render(data, defaultMaterial);
    }

    @Override
    public void render(org.example.engine.render.RenderContext ctx) {
        if (gameObject == null) {
            return;
        }

        render(gameObject.createMaterialRenderData(ctx, this));
    }

    public void render(org.example.engine.render.RenderContext ctx, Material overrideMaterial) {
        if (gameObject == null) {
            return;
        }

        render(gameObject.createMaterialRenderData(ctx, this), overrideMaterial);
    }

    public void render(MaterialRenderData data, Material overrideMaterial) {
        if (!initialized || vao == null) return;
        if (data == null) return;

        Material useMat = overrideMaterial != null ? overrideMaterial : defaultMaterial;
        if (useMat == null) return;

        useMat.bind();
        useMat.run(data);

        glBindVertexArray(vao.get(0));
        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        useMat.cleanup();
        useMat.unbind();
    }


    public void renderInstanced(MaterialRenderData data, Material overrideMaterial, int instanceCount) {
        if (!initialized || vao == null) return;
        if (data == null || instanceCount <= 0) return;

        Material useMat = overrideMaterial != null ? overrideMaterial : defaultMaterial;
        if (useMat == null) return;

        useMat.bind();
        useMat.run(data);

        glBindVertexArray(vao.get(0));
        glDrawElementsInstanced(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0, instanceCount);
        glBindVertexArray(0);

        useMat.cleanup();
        useMat.unbind();
    }

    public void debugRender(MaterialRenderData data) {
        debugRender(data, defaultMaterial);
    }

    public void debugRender(org.example.engine.render.RenderContext ctx) {
        if (gameObject == null) {
            return;
        }

        debugRender(gameObject.createMaterialRenderData(ctx, this));
    }

    public void debugRender(org.example.engine.render.RenderContext ctx, Material overrideMaterial) {
        if (gameObject == null) {
            return;
        }

        debugRender(gameObject.createMaterialRenderData(ctx, this), overrideMaterial);
    }

    public void debugRender(MaterialRenderData data, Material overrideMaterial) {
        if (!initialized || vao == null) return;
        if (data == null) return;

        Material useMat = overrideMaterial != null ? overrideMaterial : defaultMaterial;
        if (useMat == null) return;

        useMat.bind();
        useMat.run(data);

        glBindVertexArray(vao.get(0));
        glDrawElements(GL_LINES, count, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        useMat.cleanup();
        useMat.unbind();
    }

    @Override
    public boolean isRenderedByDefaultPipeline() {
        return renderedByDefaultPipeline;
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

        if (ebo != null) {
            glDeleteBuffers(ebo);
            MemoryUtil.memFree(ebo);
            ebo = null;
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

        if (boneIdBuffer != null) {
            MemoryUtil.memFree(boneIdBuffer);
            boneIdBuffer = null;
        }

        if (boneWeightBuffer != null) {
            MemoryUtil.memFree(boneWeightBuffer);
            boneWeightBuffer = null;
        }

        if (indexBuffer != null) {
            MemoryUtil.memFree(indexBuffer);
            indexBuffer = null;
        }

        positions = null;
        normals = null;
        uvs = null;
        tangents = null;
        boneIds = null;
        boneWeights = null;
        indices = null;

        count = 0;
        initialized = false;
    }

    private FloatBuffer allocateDirectFloatBuffer(int size) {
        return MemoryUtil.memAllocFloat(size);
    }
}
