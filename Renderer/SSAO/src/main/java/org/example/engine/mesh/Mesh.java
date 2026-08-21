package org.example.engine.mesh;

import org.example.engine.asset.material.MtlMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Mesh {

    LinkedHashMap<String, SubMesh> subMeshes = new LinkedHashMap<>();

    String mtllibName = null;
    LinkedHashMap<String, MtlMaterial> mtlMaterials = new LinkedHashMap<>();

    public Mesh() {
    }

    public void addTriangle(String materialName, Triangle tri) {
        if (tri == null) return;

        SubMesh sub = getOrCreateSubMesh(materialName);
        tri.materialName = sub.materialName;
        sub.appendTriangle(tri);
    }

    public SubMesh addSubMesh(SubMesh subMesh) {
        if (subMesh == null) {
            return null;
        }

        if (subMesh.materialName == null || subMesh.materialName.trim().length() == 0) {
            subMesh.materialName = "default";
        }

        subMesh.materialName = uniqueMaterialName(subMesh.materialName);
        subMeshes.put(subMesh.materialName, subMesh);
        return subMesh;
    }

    public SubMesh getOrCreateSubMesh(String materialName) {
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

    public Map<String, SubMesh> getSubMeshMap() {
        finishBuild();
        return subMeshes;
    }

    public ArrayList<SubMesh> getAllSubMeshes() {
        finishBuild();
        return new ArrayList<>(subMeshes.values());
    }

    public void finishBuild() {
        for (SubMesh subMesh : subMeshes.values()) {
            if (subMesh != null) {
                subMesh.finishBuild();
            }
        }
    }

    public MtlMaterial getMtlMaterial(String materialName) {
        return mtlMaterials.get(materialName);
    }

    public void putMtlMaterial(String materialName, MtlMaterial material) {
        if (materialName == null || material == null) {
            return;
        }
        mtlMaterials.put(materialName, material);
    }

    public int getMtlMaterialCount() {
        return mtlMaterials.size();
    }

    public boolean hasMtlMaterial(String materialName) {
        return mtlMaterials.containsKey(materialName);
    }

    public boolean hasMtlMaterials() {
        return !mtlMaterials.isEmpty();
    }

    public void setMtllibName(String mtllibName) {
        this.mtllibName = mtllibName;
    }

    public String getMtllibName() {
        return mtllibName;
    }

    public void reCaculateNormal() {
        finishBuild();
        for (SubMesh subMesh : subMeshes.values()) {
            recalculateSubMeshNormals(subMesh);
        }
    }

    private void recalculateSubMeshNormals(SubMesh subMesh) {
        if (subMesh == null || subMesh.positions == null || subMesh.positions.length == 0) {
            return;
        }

        int vertexCount = subMesh.getVertexCount();
        if (vertexCount == 0 || subMesh.indices == null || subMesh.indices.length == 0) {
            return;
        }

        float[] normals = new float[vertexCount * 3];
        HashMap<String, float[]> smoothNormals = new HashMap<>();

        for (int i = 0; i + 2 < subMesh.indices.length; i += 3) {
            int i0 = subMesh.indices[i];
            int i1 = subMesh.indices[i + 1];
            int i2 = subMesh.indices[i + 2];

            if (!validIndex(i0, vertexCount) || !validIndex(i1, vertexCount) || !validIndex(i2, vertexCount)) {
                continue;
            }

            float ax = subMesh.positions[i1 * 3] - subMesh.positions[i0 * 3];
            float ay = subMesh.positions[i1 * 3 + 1] - subMesh.positions[i0 * 3 + 1];
            float az = subMesh.positions[i1 * 3 + 2] - subMesh.positions[i0 * 3 + 2];

            float bx = subMesh.positions[i2 * 3] - subMesh.positions[i0 * 3];
            float by = subMesh.positions[i2 * 3 + 1] - subMesh.positions[i0 * 3 + 1];
            float bz = subMesh.positions[i2 * 3 + 2] - subMesh.positions[i0 * 3 + 2];

            float nx = ay * bz - az * by;
            float ny = az * bx - ax * bz;
            float nz = ax * by - ay * bx;

            addNormal(normals, i0, nx, ny, nz);
            addNormal(normals, i1, nx, ny, nz);
            addNormal(normals, i2, nx, ny, nz);

            addSmoothNormal(smoothNormals, subMesh.positions, i0, nx, ny, nz);
            addSmoothNormal(smoothNormals, subMesh.positions, i1, nx, ny, nz);
            addSmoothNormal(smoothNormals, subMesh.positions, i2, nx, ny, nz);
        }

        for (int i = 0; i < vertexCount; i++) {
            int base = i * 3;
            float[] smooth = smoothNormals.get(positionKey(subMesh.positions, i));
            float x = smooth != null ? smooth[0] : normals[base];
            float y = smooth != null ? smooth[1] : normals[base + 1];
            float z = smooth != null ? smooth[2] : normals[base + 2];
            float len = (float) Math.sqrt(x * x + y * y + z * z);
            if (len > 1e-8f) {
                normals[base] = x / len;
                normals[base + 1] = y / len;
                normals[base + 2] = z / len;
            } else {
                normals[base + 1] = 1.0f;
            }
        }

        subMesh.normals = normals;
        rebuildTangents(subMesh);
    }

    private boolean validIndex(int index, int vertexCount) {
        return index >= 0 && index < vertexCount;
    }

    private void addNormal(float[] normals, int index, float x, float y, float z) {
        int base = index * 3;
        normals[base] += x;
        normals[base + 1] += y;
        normals[base + 2] += z;
    }

    private void addSmoothNormal(HashMap<String, float[]> normals, float[] positions, int index, float x, float y, float z) {
        float[] normal = normals.computeIfAbsent(positionKey(positions, index), key -> new float[3]);
        normal[0] += x;
        normal[1] += y;
        normal[2] += z;
    }

    private String positionKey(float[] positions, int index) {
        int base = index * 3;
        int x = Math.round(positions[base] * 100000.0f);
        int y = Math.round(positions[base + 1] * 100000.0f);
        int z = Math.round(positions[base + 2] * 100000.0f);
        return x + "," + y + "," + z;
    }

    private void rebuildTangents(SubMesh subMesh) {
        int vertexCount = subMesh.getVertexCount();
        float[] tangents = new float[vertexCount * 3];

        for (int i = 0; i < vertexCount; i++) {
            int base = i * 3;
            float nx = subMesh.normals[base];
            float nz = subMesh.normals[base + 2];
            tangents[base] = -nz;
            tangents[base + 1] = 0.0f;
            tangents[base + 2] = nx;
        }

        subMesh.tangents = tangents;
    }

    private String uniqueMaterialName(String materialName) {
        if (!subMeshes.containsKey(materialName)) {
            return materialName;
        }

        int index = 1;
        String candidate = materialName + "_" + index;
        while (subMeshes.containsKey(candidate)) {
            index++;
            candidate = materialName + "_" + index;
        }
        return candidate;
    }
}
