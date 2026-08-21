package org.example.engine.importer;

import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.math.Vector4;
import org.example.engine.asset.Asset;
import org.example.engine.asset.AssetNode;
import org.example.engine.asset.animation.AnimationClip;
import org.example.engine.asset.animation.NodeAnimation;
import org.example.engine.asset.material.MaterialData;
import org.example.engine.asset.mesh.MeshData;
import org.example.engine.asset.mesh.MeshPrimitive;
import org.example.engine.asset.skin.Bone;
import org.example.engine.asset.skin.Skin;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AITexel;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.assimp.Assimp.*;

public class AssimpAssetLoader {

    private static final int MAX_BONES_PER_VERTEX = 4;

    public Asset load(String path) {
        String resolvedPath = resolvePath(path);
        int flags =
                aiProcess_Triangulate |
                aiProcess_JoinIdenticalVertices |
                aiProcess_GenSmoothNormals |
                aiProcess_ImproveCacheLocality |
                aiProcess_LimitBoneWeights;

        AIScene scene = aiImportFile(resolvedPath, flags);
        if (scene == null) {
            System.out.println("[AssimpAssetLoader] load failed: " + path);
            System.out.println("[AssimpAssetLoader] resolved path: " + resolvedPath);
            System.out.println("[AssimpAssetLoader] " + aiGetErrorString());
            return new Asset();
        }

        try {
            Asset asset = new Asset();
            asset.sourcePath = resolvedPath;
            asset.name = new File(resolvedPath).getName();

            loadMaterials(scene, asset);
            loadNodes(scene, asset);
            loadMeshes(scene, asset);
            loadAnimations(scene, asset);

            System.out.println("[AssimpAssetLoader] loaded: " + resolvedPath);
            System.out.println("[AssimpAssetLoader] nodes = " + asset.nodes.size()
                    + ", meshes = " + asset.meshes.size()
                    + ", materials = " + asset.materials.size()
                    + ", skins = " + asset.skins.size()
                    + ", animations = " + asset.animations.size());
            return asset;
        } finally {
            aiReleaseImport(scene);
        }
    }

    private String resolvePath(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.getPath();
        }

        if (path.startsWith("/")) {
            String resourcePath = path.substring(1);
            File moduleResourceFile = new File("src/main/resources", resourcePath);
            if (moduleResourceFile.exists()) {
                return moduleResourceFile.getPath();
            }

            File repoResourceFile = new File("Renderer/SSAO/src/main/resources", resourcePath);
            if (repoResourceFile.exists()) {
                return repoResourceFile.getPath();
            }
        }

        if (path.startsWith("../")) {
            File fromRepoRoot = new File(path.substring(3));
            if (fromRepoRoot.exists()) {
                return fromRepoRoot.getPath();
            }
        }

        File modelFallback = new File("../Model/GuraAnim/gura.glb");
        if (modelFallback.exists()) {
            return modelFallback.getPath();
        }

        return path;
    }

    private void loadMaterials(AIScene scene, Asset asset) {
        PointerBuffer materials = scene.mMaterials();
        HashMap<Integer, Texture> textureCache = new HashMap<>();

        for (int i = 0; i < scene.mNumMaterials(); i++) {
            MaterialData out = new MaterialData();
            AIMaterial material = AIMaterial.create(materials.get(i));
            out.name = readMaterialName(material, i);
            out.baseColorTexture = textureCache.computeIfAbsent(i, index -> loadMaterialTexture(scene, index));
            asset.materials.add(out);
        }
    }

    private String readMaterialName(AIMaterial material, int materialIndex) {
        AIString name = AIString.calloc();
        try {
            int result = aiGetMaterialString(material, AI_MATKEY_NAME, 0, 0, name);
            if (result == aiReturn_SUCCESS) {
                String value = name.dataString();
                if (value != null && value.length() > 0) {
                    return value;
                }
            }
        } finally {
            name.free();
        }

        return "material_" + materialIndex;
    }

    private void loadMeshes(AIScene scene, Asset asset) {
        PointerBuffer meshes = scene.mMeshes();
        if (meshes == null) {
            return;
        }

        for (int meshIndex = 0; meshIndex < scene.mNumMeshes(); meshIndex++) {
            AIMesh aiMesh = AIMesh.create(meshes.get(meshIndex));
            MeshData mesh = new MeshData();
            mesh.name = aiMesh.mName().dataString();
            if (mesh.name == null || mesh.name.length() == 0) {
                mesh.name = "mesh_" + meshIndex;
            }

            MeshPrimitive primitive = new MeshPrimitive();
            primitive.materialIndex = aiMesh.mMaterialIndex();
            primitive.positions = readPositions(aiMesh);
            primitive.normals = readNormals(aiMesh);
            primitive.texCoords0 = readTexCoords(aiMesh, 0);
            primitive.indices = readIndices(aiMesh);

            readSkin(aiMesh, asset, mesh.name, primitive);

            mesh.primitives.add(primitive);
            asset.meshes.add(mesh);
        }
    }

    private void readSkin(AIMesh aiMesh, Asset asset, String meshName, MeshPrimitive primitive) {
        PointerBuffer bones = aiMesh.mBones();
        if (bones == null || aiMesh.mNumBones() == 0) {
            return;
        }

        int vertexCount = primitive.getVertexCount();
        primitive.boneIds = new int[vertexCount * MAX_BONES_PER_VERTEX];
        primitive.boneWeights = new float[vertexCount * MAX_BONES_PER_VERTEX];
        for (int i = 0; i < primitive.boneIds.length; i++) {
            primitive.boneIds[i] = -1;
        }

        Skin skin = new Skin();
        skin.name = meshName + "_skin";

        for (int boneIndex = 0; boneIndex < aiMesh.mNumBones(); boneIndex++) {
            AIBone aiBone = AIBone.create(bones.get(boneIndex));
            Bone bone = new Bone();
            bone.name = aiBone.mName().dataString();
            Integer nodeIndex = asset.nodeNameToIndex.get(bone.name);
            bone.nodeIndex = nodeIndex == null ? -1 : nodeIndex;
            bone.inverseBindMatrix = toMatrix4(aiBone.mOffsetMatrix());
            skin.bones.add(bone);

            AIVertexWeight.Buffer weights = aiBone.mWeights();
            if (weights == null) {
                continue;
            }

            for (int weightIndex = 0; weightIndex < aiBone.mNumWeights(); weightIndex++) {
                AIVertexWeight weight = weights.get(weightIndex);
                addBoneInfluence(
                        primitive,
                        weight.mVertexId(),
                        boneIndex,
                        weight.mWeight()
                );
            }
        }

        normalizeBoneWeights(primitive);
        primitive.skinIndex = asset.skins.size();
        asset.skins.add(skin);

        System.out.println("[AssimpAssetLoader] skin " + skin.name
                + " bones = " + skin.bones.size()
                + ", vertices = " + vertexCount);
    }

    private void addBoneInfluence(MeshPrimitive primitive, int vertexIndex, int boneIndex, float weight) {
        if (vertexIndex < 0 || vertexIndex >= primitive.getVertexCount() || weight <= 0.0f) {
            return;
        }

        int base = vertexIndex * MAX_BONES_PER_VERTEX;
        int replaceSlot = -1;
        float smallestWeight = Float.MAX_VALUE;

        for (int i = 0; i < MAX_BONES_PER_VERTEX; i++) {
            int slot = base + i;
            if (primitive.boneIds[slot] < 0) {
                primitive.boneIds[slot] = boneIndex;
                primitive.boneWeights[slot] = weight;
                return;
            }

            if (primitive.boneWeights[slot] < smallestWeight) {
                smallestWeight = primitive.boneWeights[slot];
                replaceSlot = slot;
            }
        }

        if (replaceSlot >= 0 && weight > smallestWeight) {
            primitive.boneIds[replaceSlot] = boneIndex;
            primitive.boneWeights[replaceSlot] = weight;
        }
    }

    private void normalizeBoneWeights(MeshPrimitive primitive) {
        for (int vertexIndex = 0; vertexIndex < primitive.getVertexCount(); vertexIndex++) {
            int base = vertexIndex * MAX_BONES_PER_VERTEX;
            float sum = 0.0f;

            for (int i = 0; i < MAX_BONES_PER_VERTEX; i++) {
                float weight = primitive.boneWeights[base + i];
                if (weight > 0.0f) {
                    sum += weight;
                }
            }

            if (sum <= 0.0f) {
                continue;
            }

            for (int i = 0; i < MAX_BONES_PER_VERTEX; i++) {
                primitive.boneWeights[base + i] /= sum;
            }
        }
    }

    private void loadNodes(AIScene scene, Asset asset) {
        AINode rootNode = scene.mRootNode();
        if (rootNode == null) {
            return;
        }

        int rootIndex = readNodeRecursive(rootNode, -1, asset);
        if (rootIndex >= 0) {
            asset.rootNodeIndices.add(rootIndex);
        }
    }

    private int readNodeRecursive(AINode aiNode, int parentIndex, Asset asset) {
        AssetNode node = new AssetNode();
        node.name = aiNode.mName().dataString();
        node.parentIndex = parentIndex;
        node.localTransform = toMatrix4(aiNode.mTransformation());

        IntBuffer meshIndices = aiNode.mMeshes();
        if (meshIndices != null) {
            for (int i = 0; i < aiNode.mNumMeshes(); i++) {
                int meshIndex = meshIndices.get(i);
                node.meshIndices.add(meshIndex);
                if (node.meshIndex < 0) {
                    node.meshIndex = meshIndex;
                }
            }
        }

        int nodeIndex = asset.nodes.size();
        asset.nodes.add(node);
        if (node.name != null && node.name.length() > 0) {
            asset.nodeNameToIndex.put(node.name, nodeIndex);
        }

        if (parentIndex >= 0) {
            asset.nodes.get(parentIndex).childIndices.add(nodeIndex);
        }

        PointerBuffer children = aiNode.mChildren();
        if (children != null) {
            for (int i = 0; i < aiNode.mNumChildren(); i++) {
                readNodeRecursive(AINode.create(children.get(i)), nodeIndex, asset);
            }
        }

        return nodeIndex;
    }

    private Matrix4 toMatrix4(AIMatrix4x4 aiMatrix) {
        Matrix4 out = Matrix4.Identity();

        out.set(0, 0, aiMatrix.a1());
        out.set(0, 1, aiMatrix.a2());
        out.set(0, 2, aiMatrix.a3());
        out.set(0, 3, aiMatrix.a4());
        out.set(1, 0, aiMatrix.b1());
        out.set(1, 1, aiMatrix.b2());
        out.set(1, 2, aiMatrix.b3());
        out.set(1, 3, aiMatrix.b4());
        out.set(2, 0, aiMatrix.c1());
        out.set(2, 1, aiMatrix.c2());
        out.set(2, 2, aiMatrix.c3());
        out.set(2, 3, aiMatrix.c4());
        out.set(3, 0, aiMatrix.d1());
        out.set(3, 1, aiMatrix.d2());
        out.set(3, 2, aiMatrix.d3());
        out.set(3, 3, aiMatrix.d4());

        return out;
    }

    private float[] readPositions(AIMesh mesh) {
        float[] out = new float[mesh.mNumVertices() * 3];
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            var value = mesh.mVertices().get(i);
            int base = i * 3;
            out[base] = value.x();
            out[base + 1] = value.y();
            out[base + 2] = value.z();
        }
        return out;
    }

    private float[] readNormals(AIMesh mesh) {
        if (mesh.mNormals() == null) {
            return new float[0];
        }

        float[] out = new float[mesh.mNumVertices() * 3];
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            var value = mesh.mNormals().get(i);
            int base = i * 3;
            out[base] = value.x();
            out[base + 1] = value.y();
            out[base + 2] = value.z();
        }
        return out;
    }

    private float[] readTexCoords(AIMesh mesh, int channel) {
        if (mesh.mTextureCoords(channel) == null) {
            return new float[0];
        }

        float[] out = new float[mesh.mNumVertices() * 2];
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            var value = mesh.mTextureCoords(channel).get(i);
            int base = i * 2;
            out[base] = value.x();
            out[base + 1] = value.y();
        }
        return out;
    }

    private int[] readIndices(AIMesh mesh) {
        int[] out = new int[mesh.mNumFaces() * 3];
        int cursor = 0;
        for (int faceIndex = 0; faceIndex < mesh.mNumFaces(); faceIndex++) {
            AIFace face = mesh.mFaces().get(faceIndex);
            if (face.mNumIndices() != 3) {
                continue;
            }

            IntBuffer indices = face.mIndices();
            out[cursor++] = indices.get(0);
            out[cursor++] = indices.get(1);
            out[cursor++] = indices.get(2);
        }

        if (cursor == out.length) {
            return out;
        }

        int[] compact = new int[cursor];
        System.arraycopy(out, 0, compact, 0, cursor);
        return compact;
    }

    private void loadAnimations(AIScene scene, Asset asset) {
        PointerBuffer animations = scene.mAnimations();
        if (animations == null) {
            return;
        }

        for (int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation aiAnimation = AIAnimation.create(animations.get(i));
            AnimationClip clip = new AnimationClip();
            clip.name = aiAnimation.mName().dataString();
            clip.duration = aiAnimation.mDuration();
            clip.ticksPerSecond = aiAnimation.mTicksPerSecond();

            PointerBuffer channels = aiAnimation.mChannels();
            if (channels != null) {
                for (int j = 0; j < aiAnimation.mNumChannels(); j++) {
                    clip.channels.add(readNodeAnimation(AINodeAnim.create(channels.get(j)), asset));
                }
            }

            asset.animations.add(clip);
        }
    }

    private NodeAnimation readNodeAnimation(AINodeAnim channel, Asset asset) {
        NodeAnimation out = new NodeAnimation();
        out.nodeName = channel.mNodeName().dataString();
        Integer nodeIndex = asset.nodeNameToIndex.get(out.nodeName);
        if (nodeIndex != null) {
            out.nodeIndex = nodeIndex;
        } else {
            System.out.println("[AssimpAssetLoader] animation channel node not found: " + out.nodeName);
        }

        AIVectorKey.Buffer positionKeys = channel.mPositionKeys();
        if (positionKeys != null) {
            for (int i = 0; i < channel.mNumPositionKeys(); i++) {
                AIVectorKey key = positionKeys.get(i);
                var value = key.mValue();
                out.positionKeys.add(new NodeAnimation.VectorKeyframe(
                        key.mTime(),
                        new Vector3(value.x(), value.y(), value.z())
                ));
            }
        }

        AIQuatKey.Buffer rotationKeys = channel.mRotationKeys();
        if (rotationKeys != null) {
            for (int i = 0; i < channel.mNumRotationKeys(); i++) {
                AIQuatKey key = rotationKeys.get(i);
                var value = key.mValue();
                out.rotationKeys.add(new NodeAnimation.QuaternionKeyframe(
                        key.mTime(),
                        new Vector4(value.x(), value.y(), value.z(), value.w())
                ));
            }
        }

        AIVectorKey.Buffer scaleKeys = channel.mScalingKeys();
        if (scaleKeys != null) {
            for (int i = 0; i < channel.mNumScalingKeys(); i++) {
                AIVectorKey key = scaleKeys.get(i);
                var value = key.mValue();
                out.scaleKeys.add(new NodeAnimation.VectorKeyframe(
                        key.mTime(),
                        new Vector3(value.x(), value.y(), value.z())
                ));
            }
        }

        return out;
    }

    private Texture loadMaterialTexture(AIScene scene, int materialIndex) {
        PointerBuffer materials = scene.mMaterials();
        if (materials == null || materialIndex < 0 || materialIndex >= scene.mNumMaterials()) {
            return null;
        }

        AIMaterial material = AIMaterial.create(materials.get(materialIndex));
        AIString texturePath = AIString.calloc();

        try {
            int result = readTextureRef(material, aiTextureType_DIFFUSE, texturePath);

            if (result != aiReturn_SUCCESS) {
                result = readTextureRef(material, aiTextureType_BASE_COLOR, texturePath);
            }

            if (result != aiReturn_SUCCESS) {
                result = readTextureRef(material, aiTextureType_EMISSIVE, texturePath);
            }

            if (result != aiReturn_SUCCESS) {
                result = readTextureRef(material, aiTextureType_EMISSION_COLOR, texturePath);
            }

            if (result != aiReturn_SUCCESS) {
                System.out.println("[AssimpAssetLoader] no diffuse/baseColor/emissive texture for material_" + materialIndex);
                return null;
            }

            String path = texturePath.dataString();
            System.out.println("[AssimpAssetLoader] material_" + materialIndex + " texture ref = " + path);

            if (path.startsWith("*")) {
                return loadEmbeddedTexture(scene, path);
            }

            return new Texture(path, true, true);
        } finally {
            texturePath.free();
        }
    }

    private int readTextureRef(AIMaterial material, int textureType, AIString texturePath) {
        return aiGetMaterialTexture(
                material,
                textureType,
                0,
                texturePath,
                (IntBuffer) null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private Texture loadEmbeddedTexture(AIScene scene, String ref) {
        PointerBuffer textures = scene.mTextures();
        if (textures == null) {
            return null;
        }

        int textureIndex = parseEmbeddedTextureIndex(ref);
        if (textureIndex < 0 || textureIndex >= scene.mNumTextures()) {
            System.out.println("[AssimpAssetLoader] invalid embedded texture ref: " + ref);
            return null;
        }

        AITexture aiTexture = AITexture.create(textures.get(textureIndex));

        if (aiTexture.mHeight() == 0) {
            ByteBuffer encodedBytes = MemoryUtil.memByteBuffer(
                    aiTexture.pcData().address(),
                    aiTexture.mWidth()
            );

            return new Texture(encodedBytes, true, true);
        }

        AITexel.Buffer texels = aiTexture.pcData();
        if (texels == null) {
            return null;
        }

        ByteBuffer pixels = MemoryUtil.memAlloc(aiTexture.mWidth() * aiTexture.mHeight() * 4);
        for (int i = 0; i < aiTexture.mWidth() * aiTexture.mHeight(); i++) {
            AITexel texel = texels.get(i);
            pixels.put(texel.r());
            pixels.put(texel.g());
            pixels.put(texel.b());
            pixels.put(texel.a());
        }
        pixels.flip();

        Texture texture = new Texture();
        texture.setUseMipmap(true);
        texture.setRawRGBA(pixels, aiTexture.mWidth(), aiTexture.mHeight());
        MemoryUtil.memFree(pixels);

        return texture;
    }

    private int parseEmbeddedTextureIndex(String ref) {
        try {
            return Integer.parseInt(ref.substring(1));
        } catch (Exception e) {
            return -1;
        }
    }
}
