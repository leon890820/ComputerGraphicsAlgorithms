package org.example.engine.importer;

import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;
import org.example.engine.asset.Asset;
import org.example.engine.asset.material.MaterialData;
import org.example.engine.asset.mesh.MeshData;
import org.example.engine.asset.mesh.MeshPrimitive;

public class AssetMeshAdapter {

    public Mesh toMesh(Asset asset) {
        Mesh out = new Mesh();
        if (asset == null) {
            return out;
        }

        for (MeshData mesh : asset.meshes) {
            if (mesh == null) {
                continue;
            }

            for (MeshPrimitive primitive : mesh.primitives) {
                if (primitive == null) {
                    continue;
                }

                if (isOutlineMaterial(asset, primitive)) {
                    System.out.println("[AssetMeshAdapter] skipped outline material.");
                    continue;
                }

                out.addSubMesh(toSubMesh(asset, primitive));
            }
        }

        System.out.println("[AssetMeshAdapter] converted subMeshes = " + out.getAllSubMeshes().size());
        out.printSubMeshInfo();
        return out;
    }

    private SubMesh toSubMesh(Asset asset, MeshPrimitive primitive) {
        String materialName = "material_" + primitive.materialIndex;
        SubMesh subMesh = new SubMesh(materialName);
        subMesh.setGeometry(
                primitive.positions,
                primitive.normals,
                primitive.texCoords0,
                primitive.hasIndices() ? primitive.indices : null,
                primitive.boneIds,
                primitive.boneWeights,
                primitive.skinIndex
        );

        MaterialData material = getMaterial(asset, primitive.materialIndex);
        if (material != null && material.baseColorTexture != null && material.baseColorTexture.isUploaded()) {
            subMesh.textureKa = material.baseColorTexture;
        }

        return subMesh;
    }

    private MaterialData getMaterial(Asset asset, int materialIndex) {
        if (asset == null || materialIndex < 0 || materialIndex >= asset.materials.size()) {
            return null;
        }
        return asset.materials.get(materialIndex);
    }

    private boolean isOutlineMaterial(Asset asset, MeshPrimitive primitive) {
        if (asset == null || primitive == null) {
            return false;
        }

        MaterialData material = getMaterial(asset, primitive.materialIndex);
        if (material == null || material.name == null) {
            return false;
        }

        return material.name.toLowerCase().contains("outline");
    }
}
