package org.example.engine.mesh;

import org.example.engine.gl.Texture;
import org.example.engine.math.Vector3;

import java.util.ArrayList;

public class SubMesh {

    public String materialName;
    public ArrayList<Triangle> triangles = new ArrayList<>();

    public Texture textureKa;
    public int skinIndex = -1;

    public SubMesh(String materialName) {
        this.materialName = materialName;
    }

    public float[] getTrianglePosition() {
        return exportTriangleVectors(3, new TriangleVectorGetter() {
            @Override
            public Vector3 get(Triangle tri, int vertexIndex) {
                return tri.verts[vertexIndex];
            }
        });
    }

    public float[] getTriangleNormal() {
        return exportTriangleVectors(3, new TriangleVectorGetter() {
            @Override
            public Vector3 get(Triangle tri, int vertexIndex) {
                if (tri.normals != null && tri.normals[vertexIndex] != null) {
                    return tri.normals[vertexIndex];
                }
                return new Vector3(0, 1, 0);
            }
        });
    }

    public float[] getTriangleTangent() {
        return exportTriangleVectors(3, new TriangleVectorGetter() {
            @Override
            public Vector3 get(Triangle tri, int vertexIndex) {
                if (tri.tangents != null && tri.tangents[vertexIndex] != null) {
                    return tri.tangents[vertexIndex];
                }
                return new Vector3(1, 0, 0);
            }
        });
    }

    public float[] getTriangleUV() {
        return exportTriangleVectors(2, new TriangleVectorGetter() {
            @Override
            public Vector3 get(Triangle tri, int vertexIndex) {
                if (tri.uvs != null && tri.uvs[vertexIndex] != null) {
                    return tri.uvs[vertexIndex];
                }
                return new Vector3(0, 0, 0);
            }
        });
    }

    public int[] getTriangleBoneIds() {
        int[] out = new int[triangles.size() * 3 * 4];

        for (int i = 0; i < triangles.size(); i++) {
            Triangle tri = triangles.get(i);
            for (int j = 0; j < 12; j++) {
                out[i * 12 + j] = tri.boneIds == null ? 0 : Math.max(0, tri.boneIds[j]);
            }
        }

        return out;
    }

    public float[] getTriangleBoneWeights() {
        float[] out = new float[triangles.size() * 3 * 4];

        for (int i = 0; i < triangles.size(); i++) {
            Triangle tri = triangles.get(i);
            for (int j = 0; j < 12; j++) {
                out[i * 12 + j] = tri.boneWeights == null ? 0.0f : tri.boneWeights[j];
            }
        }

        return out;
    }

    public boolean hasSkinWeights() {
        if (skinIndex < 0) {
            return false;
        }

        for (Triangle tri : triangles) {
            if (tri == null || tri.boneWeights == null) {
                continue;
            }

            for (float weight : tri.boneWeights) {
                if (weight > 0.0f) {
                    return true;
                }
            }
        }

        return false;
    }

    private float[] exportTriangleVectors(int componentCount, TriangleVectorGetter getter) {
        float[] out = new float[triangles.size() * 3 * componentCount];

        for (int i = 0; i < triangles.size(); i++) {
            Triangle tri = triangles.get(i);

            for (int j = 0; j < 3; j++) {
                Vector3 value = getter.get(tri, j);
                if (value == null) {
                    value = new Vector3(0, 0, 0);
                }

                int base = i * 3 * componentCount + j * componentCount;

                out[base + 0] = value.x;
                out[base + 1] = value.y;

                if (componentCount >= 3) {
                    out[base + 2] = value.z;
                }
            }
        }

        return out;
    }

    @Override
    public String toString() {
        return "SubMesh(material=" + materialName + ", triangles=" + triangles.size() + ")";
    }
}
