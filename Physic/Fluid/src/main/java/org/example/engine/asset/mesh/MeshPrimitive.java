package org.example.engine.asset.mesh;

public class MeshPrimitive {
    public float[] positions = new float[0];
    public float[] normals = new float[0];
    public float[] tangents = new float[0];
    public float[] texCoords0 = new float[0];
    public float[] texCoords1 = new float[0];
    public int[] indices = new int[0];

    public int materialIndex = -1;
    public int skinIndex = -1;

    public int[] boneIds = new int[0];
    public float[] boneWeights = new float[0];

    public int getVertexCount() {
        return positions.length / 3;
    }

    public boolean hasIndices() {
        return indices.length > 0;
    }

    public boolean hasSkinWeights() {
        return boneIds.length > 0 && boneWeights.length > 0;
    }
}
