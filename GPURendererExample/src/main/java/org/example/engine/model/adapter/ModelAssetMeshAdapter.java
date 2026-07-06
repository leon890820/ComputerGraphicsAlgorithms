package org.example.engine.model.adapter;

import org.example.engine.model.ModelAsset;
import org.example.engine.model.material.ModelMaterial;
import org.example.engine.model.mesh.ModelMesh;
import org.example.engine.model.mesh.ModelPrimitive;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;
import org.example.engine.mesh.Triangle;
import org.example.engine.math.Vector3;

public class ModelAssetMeshAdapter {

    private boolean skipGuraOutlineMaterial = true;

    public ModelAssetMeshAdapter setSkipGuraOutlineMaterial(boolean skip) {
        skipGuraOutlineMaterial = skip;
        return this;
    }

    public Mesh toMesh(ModelAsset asset) {
        Mesh out = new Mesh();
        if (asset == null) {
            return out;
        }

        int vertexBase = 0;
        for (ModelMesh mesh : asset.meshes) {
            if (mesh == null) {
                continue;
            }

            for (ModelPrimitive primitive : mesh.primitives) {
                if (primitive == null) {
                    continue;
                }

                if (skipGuraOutlineMaterial && primitive.materialIndex == 1) {
                    System.out.println("[ModelAssetMeshAdapter] skipped material_1 for legacy Gura outline behavior.");
                    vertexBase += primitive.getVertexCount();
                    continue;
                }

                appendPrimitive(out, asset, primitive, vertexBase);
                vertexBase += primitive.getVertexCount();
            }
        }

        System.out.println("[ModelAssetMeshAdapter] converted subMeshes = " + out.getAllSubMeshes().size());
        out.printSubMeshInfo();
        return out;
    }

    private void appendPrimitive(Mesh out, ModelAsset asset, ModelPrimitive primitive, int vertexBase) {
        String materialName = "material_" + primitive.materialIndex;
        int[] indices = primitive.hasIndices() ? primitive.indices : buildSequentialIndices(primitive.getVertexCount());

        for (int i = 0; i + 2 < indices.length; i += 3) {
            int i0 = indices[i];
            int i1 = indices[i + 1];
            int i2 = indices[i + 2];

            Vector3[] verts = new Vector3[] {
                    readPosition(primitive, i0),
                    readPosition(primitive, i1),
                    readPosition(primitive, i2)
            };
            Vector3[] uvs = new Vector3[] {
                    readUv(primitive, i0),
                    readUv(primitive, i1),
                    readUv(primitive, i2)
            };
            Vector3[] normals = new Vector3[] {
                    readNormal(primitive, i0),
                    readNormal(primitive, i1),
                    readNormal(primitive, i2)
            };
            int[] vertexIndices = new int[] {
                    vertexBase + i0,
                    vertexBase + i1,
                    vertexBase + i2
            };

            Vector3 faceNormal = Vector3.cross(
                    verts[1].sub(verts[0]),
                    verts[2].sub(verts[0])
            ).unit_vector();

            for (int n = 0; n < 3; n++) {
                if (normals[n] == null) {
                    normals[n] = faceNormal;
                }
            }

            out.addTriangle(materialName, new Triangle(verts, uvs, normals, vertexIndices));
        }

        SubMesh subMesh = out.getSubMesh(materialName);
        ModelMaterial material = getMaterial(asset, primitive.materialIndex);
        if (subMesh != null && material != null && material.baseColorTexture != null && material.baseColorTexture.isUploaded()) {
            subMesh.textureKa = material.baseColorTexture;
        }
    }

    private int[] buildSequentialIndices(int vertexCount) {
        int[] indices = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            indices[i] = i;
        }
        return indices;
    }

    private ModelMaterial getMaterial(ModelAsset asset, int materialIndex) {
        if (asset == null || materialIndex < 0 || materialIndex >= asset.materials.size()) {
            return null;
        }
        return asset.materials.get(materialIndex);
    }

    private Vector3 readPosition(ModelPrimitive primitive, int index) {
        int base = index * 3;
        if (base + 2 >= primitive.positions.length) {
            return new Vector3(0, 0, 0);
        }
        return new Vector3(
                primitive.positions[base],
                primitive.positions[base + 1],
                primitive.positions[base + 2]
        );
    }

    private Vector3 readNormal(ModelPrimitive primitive, int index) {
        int base = index * 3;
        if (base + 2 >= primitive.normals.length) {
            return null;
        }
        return new Vector3(
                primitive.normals[base],
                primitive.normals[base + 1],
                primitive.normals[base + 2]
        );
    }

    private Vector3 readUv(ModelPrimitive primitive, int index) {
        int base = index * 2;
        if (base + 1 >= primitive.texCoords0.length) {
            return new Vector3(0, 0, 0);
        }
        return new Vector3(
                primitive.texCoords0[base],
                primitive.texCoords0[base + 1],
                0
        );
    }
}
