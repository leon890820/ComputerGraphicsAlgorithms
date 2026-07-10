package org.example.engine.mesh;

import org.example.engine.gl.Texture;
import org.example.engine.math.Vector3;

import java.util.ArrayList;

public class SubMesh {

    public String materialName;

    public float[] positions = new float[0];
    public float[] normals = new float[0];
    public float[] tangents = new float[0];
    public float[] uvs = new float[0];
    public int[] indices = new int[0];
    public int[] boneIds = new int[0];
    public float[] boneWeights = new float[0];
    public float[] prtCoefficients = new float[0];
    public int prtBands = 0;
    public int prtSampleCount = 0;

    public Texture textureKa;
    public int skinIndex = -1;

    private ArrayList<Float> positionBuild;
    private ArrayList<Float> normalBuild;
    private ArrayList<Float> tangentBuild;
    private ArrayList<Float> uvBuild;
    private ArrayList<Integer> indexBuild;
    private ArrayList<Integer> boneIdBuild;
    private ArrayList<Float> boneWeightBuild;

    public SubMesh(String materialName) {
        this.materialName = materialName;
    }

    public int getVertexCount() {
        if (positionBuild != null) {
            return positionBuild.size() / 3;
        }
        return positions == null ? 0 : positions.length / 3;
    }

    public int getIndexCount() {
        if (indexBuild != null) {
            return indexBuild.size();
        }
        return indices == null ? 0 : indices.length;
    }

    public void setGeometry(
            float[] positions,
            float[] normals,
            float[] uvs,
            int[] indices,
            int[] boneIds,
            float[] boneWeights,
            int skinIndex
    ) {
        this.positions = safeFloatArray(positions);
        this.normals = safeFloatArray(normals);
        this.uvs = safeFloatArray(uvs);
        this.indices = indices != null && indices.length > 0 ? indices : buildSequentialIndices(getVertexCount());
        this.boneIds = safeIntArray(boneIds);
        this.boneWeights = safeFloatArray(boneWeights);
        this.skinIndex = skinIndex;
        clearBuildBuffers();
        rebuildFallbackChannels();
    }

    public void appendTriangle(Triangle tri) {
        if (tri == null || tri.verts == null || tri.verts.length < 3) {
            return;
        }

        ensureBuildBuffers();
        int vertexBase = positionBuild.size() / 3;

        for (int i = 0; i < 3; i++) {
            addVector(positionBuild, tri.verts[i], new Vector3(0, 0, 0), 3);
            addVector(normalBuild, readVector(tri.normals, i, new Vector3(0, 1, 0)), new Vector3(0, 1, 0), 3);
            addVector(tangentBuild, readVector(tri.tangents, i, new Vector3(1, 0, 0)), new Vector3(1, 0, 0), 3);
            addVector(uvBuild, readVector(tri.uvs, i, new Vector3(0, 0, 0)), new Vector3(0, 0, 0), 2);
        }

        indexBuild.add(vertexBase);
        indexBuild.add(vertexBase + 1);
        indexBuild.add(vertexBase + 2);

        for (int i = 0; i < 12; i++) {
            boneIdBuild.add(tri.boneIds != null && i < tri.boneIds.length ? tri.boneIds[i] : 0);
            boneWeightBuild.add(tri.boneWeights != null && i < tri.boneWeights.length ? tri.boneWeights[i] : 0.0f);
        }
    }

    public void finishBuild() {
        if (positionBuild == null) {
            return;
        }

        positions = toFloatArray(positionBuild);
        normals = toFloatArray(normalBuild);
        tangents = toFloatArray(tangentBuild);
        uvs = toFloatArray(uvBuild);
        indices = toIntArray(indexBuild);
        boneIds = toIntArray(boneIdBuild);
        boneWeights = toFloatArray(boneWeightBuild);

        clearBuildBuffers();
        rebuildFallbackChannels();
    }

    public boolean hasSkinWeights() {
        if (skinIndex < 0 || boneIds == null || boneWeights == null) {
            return false;
        }

        for (float weight : boneWeights) {
            if (weight > 0.0f) {
                return true;
            }
        }

        return false;
    }

    private void rebuildFallbackChannels() {
        int vertexCount = getVertexCount();

        if (normals.length != vertexCount * 3) {
            normals = new float[vertexCount * 3];
            for (int i = 0; i < vertexCount; i++) {
                normals[i * 3 + 1] = 1.0f;
            }
        }

        if (tangents.length != vertexCount * 3) {
            tangents = new float[vertexCount * 3];
            for (int i = 0; i < vertexCount; i++) {
                tangents[i * 3] = 1.0f;
            }
        }

        if (uvs.length != vertexCount * 2) {
            uvs = new float[vertexCount * 2];
        }

        if (boneIds.length != vertexCount * 4) {
            boneIds = new int[vertexCount * 4];
        }

        if (boneWeights.length != vertexCount * 4) {
            boneWeights = new float[vertexCount * 4];
        }
    }

    private int[] buildSequentialIndices(int vertexCount) {
        int[] out = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            out[i] = i;
        }
        return out;
    }

    private Vector3 readVector(Vector3[] values, int index, Vector3 fallback) {
        if (values != null && index >= 0 && index < values.length && values[index] != null) {
            return values[index];
        }
        return fallback;
    }

    private void ensureBuildBuffers() {
        if (positionBuild != null) {
            return;
        }

        positionBuild = new ArrayList<>();
        normalBuild = new ArrayList<>();
        tangentBuild = new ArrayList<>();
        uvBuild = new ArrayList<>();
        indexBuild = new ArrayList<>();
        boneIdBuild = new ArrayList<>();
        boneWeightBuild = new ArrayList<>();

        append(positionBuild, positions);
        append(normalBuild, normals);
        append(tangentBuild, tangents);
        append(uvBuild, uvs);
        append(indexBuild, indices);
        append(boneIdBuild, boneIds);
        append(boneWeightBuild, boneWeights);
    }

    private void clearBuildBuffers() {
        positionBuild = null;
        normalBuild = null;
        tangentBuild = null;
        uvBuild = null;
        indexBuild = null;
        boneIdBuild = null;
        boneWeightBuild = null;
    }

    private void addVector(ArrayList<Float> out, Vector3 value, Vector3 fallback, int componentCount) {
        Vector3 v = value == null ? fallback : value;
        out.add(v.x);
        out.add(v.y);
        if (componentCount >= 3) {
            out.add(v.z);
        }
    }

    private void append(ArrayList<Float> out, float[] values) {
        if (values == null) return;
        for (float value : values) {
            out.add(value);
        }
    }

    private void append(ArrayList<Integer> out, int[] values) {
        if (values == null) return;
        for (int value : values) {
            out.add(value);
        }
    }

    private float[] toFloatArray(ArrayList<Float> values) {
        if (values == null || values.size() == 0) {
            return new float[0];
        }

        float[] out = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }

    private int[] toIntArray(ArrayList<Integer> values) {
        if (values == null || values.size() == 0) {
            return new int[0];
        }

        int[] out = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            out[i] = values.get(i);
        }
        return out;
    }

    private float[] safeFloatArray(float[] values) {
        return values == null ? new float[0] : values;
    }

    private int[] safeIntArray(int[] values) {
        return values == null ? new int[0] : values;
    }

    @Override
    public String toString() {
        return "SubMesh(material=" + materialName
                + ", vertices=" + getVertexCount()
                + ", indices=" + getIndexCount()
                + ")";
    }
}
