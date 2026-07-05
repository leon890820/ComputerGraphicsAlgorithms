package org.example.engine.mesh;

import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;

public class Triangle {

    public String materialName = "default";

    public Vector3[] verts;
    public Vector3[] uvs;
    public Vector3[] normals;
    public Vector3[] tangents;

    // shared vertex index（給 smooth normal 用）
    public int[] vertexIndices;

    public Vector3 center;

    public Triangle(Vector3[] verts, Vector3[] uvs, Vector3[] normals, int[] vertexIndices) {
        this.verts = verts;
        this.uvs = uvs;
        this.normals = normals;
        this.vertexIndices = vertexIndices;

        // 中心點
        this.center = (verts[0].add(verts[1]).add(verts[2])).mult(1.0f / 3.0f);

        calculateTangent();
    }

    public void calculateTangent() {
        tangents = new Vector3[3];

        for (int i = 0; i < 3; i++) {
            Vector3 n = (normals != null && normals[i] != null)
                    ? normals[i]
                    : new Vector3(0, 1, 0);

            // 簡單 fallback tangent（你原本寫法）
            tangents[i] = new Vector3(-n.z, 0.0f, n.x);
        }
    }

    public boolean intersection(Vector3 o, Vector3 dir, Matrix4 ltw) {

        Vector3 v0 = ltw.transformPoint(verts[0]);
        Vector3 v1 = ltw.transformPoint(verts[1]);
        Vector3 v2 = ltw.transformPoint(verts[2]);

        Vector3 e1 = Vector3.sub(v1, v0);
        Vector3 e2 = Vector3.sub(v2, v0);
        Vector3 s = Vector3.sub(o, v0);

        Vector3 s1 = Vector3.cross(dir, e2);
        Vector3 s2 = Vector3.cross(s, e1);

        float denom = Vector3.dot(s1, e1);

        // ⭐ 防止除0 crash（你原本沒檢查）
        if (Math.abs(denom) < 1e-8f) return false;

        float inv = 1.0f / denom;

        float t = Vector3.dot(s2, e2) * inv;
        float b1 = Vector3.dot(s1, s) * inv;
        float b2 = Vector3.dot(s2, dir) * inv;

        return b1 > 0.01f &&
                b2 > 0.01f &&
                (1.0f - b1 - b2) > 0.01f &&
                t > 0.01f;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("Material: ").append(materialName).append("\n");

        s.append("Vertices:\n");
        if (verts != null) {
            for (Vector3 v : verts) {
                s.append(v == null ? "null" : v.toString()).append("\n");
            }
        }

        s.append("UVs:\n");
        if (uvs != null) {
            for (Vector3 v : uvs) {
                s.append(v == null ? "null" : v.toString()).append("\n");
            }
        }

        return s.toString();
    }
}