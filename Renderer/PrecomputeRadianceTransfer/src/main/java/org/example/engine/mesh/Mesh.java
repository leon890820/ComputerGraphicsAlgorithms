package org.example.engine.mesh;

import org.example.engine.mesh.MtlMaterial;
import org.example.engine.math.Vector3;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Mesh {

    ArrayList<Triangle> triangles = new ArrayList<>();
    LinkedHashMap<String, SubMesh> subMeshes = new LinkedHashMap<>();

    String mtllibName = null;
    LinkedHashMap<String, MtlMaterial> mtlMaterials = new LinkedHashMap<>();

    public Mesh() {
    }

    public void addTriangle(String materialName, Triangle tri) {
        if (tri == null) return;

        triangles.add(tri);

        if (materialName == null || materialName.trim().length() == 0) {
            materialName = "default";
        }

        SubMesh sub = getOrCreateSubMesh(materialName);
        tri.materialName = materialName;
        sub.triangles.add(tri);
    }

    private SubMesh getOrCreateSubMesh(String materialName) {
        if (materialName == null || materialName.trim().length() == 0) {
            materialName = "default";
        }

        SubMesh sub = subMeshes.get(materialName);
        if (sub == null) {
            sub = new SubMesh(materialName);
            subMeshes.put(materialName, sub);
        }
        return sub;
    }

    public SubMesh getSubMesh(String materialName) {
        return subMeshes.get(materialName);
    }

    public ArrayList<SubMesh> getAllSubMeshes() {
        return new ArrayList<>(subMeshes.values());
    }

    public MtlMaterial getMtlMaterial(String materialName) {
        return mtlMaterials.get(materialName);
    }

    public boolean hasMtlMaterial(String materialName) {
        return mtlMaterials.containsKey(materialName);
    }

    public void printSubMeshInfo() {
        System.out.println("SubMesh count: " + subMeshes.size());
        for (String key : subMeshes.keySet()) {
            SubMesh sub = subMeshes.get(key);
            System.out.println("material = " + key + ", triangles = " + sub.triangles.size());
        }
    }

    public void printTextureKaInfo() {
        for (String key : subMeshes.keySet()) {
            SubMesh sub = subMeshes.get(key);
            boolean hasTex = sub != null && sub.textureKa != null && sub.textureKa.isUploaded();
            System.out.println("material = " + key + ", has map_Ka texture = " + hasTex);
        }
    }

    public void printMtlInfo() {
        System.out.println("mtllib = " + mtllibName);
        System.out.println("mtl material count = " + mtlMaterials.size());
        for (String key : mtlMaterials.keySet()) {
            MtlMaterial m = mtlMaterials.get(key);
            System.out.println("material = " + key + ", map_Ka = " + m.mapKa);
        }
    }

    void reCaculateNormal() {
        if (triangles == null || triangles.size() == 0) return;

        int maxIndex = -1;
        for (Triangle tri : triangles) {
            for (int i = 0; i < 3; i++) {
                if (tri.vertexIndices[i] > maxIndex) {
                    maxIndex = tri.vertexIndices[i];
                }
            }
        }

        if (maxIndex < 0) return;

        Vector3[] smoothNormals = new Vector3[maxIndex + 1];
        for (int i = 0; i < smoothNormals.length; i++) {
            smoothNormals[i] = new Vector3();
        }

        for (Triangle tri : triangles) {
            Vector3 n = Vector3.cross(
                    tri.verts[1].sub(tri.verts[0]),
                    tri.verts[2].sub(tri.verts[0])
            );

            for (int j = 0; j < 3; j++) {
                int idx = tri.vertexIndices[j];
                if (idx >= 0 && idx < smoothNormals.length) {
                    smoothNormals[idx] = smoothNormals[idx].add(n);
                }
            }
        }

        for (Triangle tri : triangles) {
            for (int j = 0; j < 3; j++) {
                int idx = tri.vertexIndices[j];
                if (idx >= 0 && idx < smoothNormals.length) {
                    tri.normals[j] = smoothNormals[idx].unit_vector();
                }
            }
            tri.calculateTangent();
        }
    }
}