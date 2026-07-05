package org.example.engine.mesh;

import org.example.engine.gl.Texture;
import org.example.engine.math.Vector3;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AITexel;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedHashSet;

import static org.lwjgl.assimp.Assimp.*;

public class AssimpLoader {

    public Mesh load(String path) {
        String resolvedPath = resolvePath(path);
        int flags =
                aiProcess_Triangulate |
                aiProcess_JoinIdenticalVertices |
                aiProcess_GenSmoothNormals |
                aiProcess_ImproveCacheLocality |
                aiProcess_LimitBoneWeights;

        AIScene scene = aiImportFile(resolvedPath, flags);
        if (scene == null) {
            System.out.println("[AssimpLoader] load failed: " + path);
            System.out.println("[AssimpLoader] resolved path: " + resolvedPath);
            System.out.println("[AssimpLoader] " + aiGetErrorString());
            return new Mesh();
        }

        try {
            printSceneInfo(resolvedPath, scene);
            return buildMesh(scene);
        } finally {
            aiReleaseImport(scene);
        }
    }

    private String resolvePath(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.getPath();
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

    private Mesh buildMesh(AIScene scene) {
        Mesh out = new Mesh();
        PointerBuffer meshes = scene.mMeshes();
        HashMap<Integer, Texture> textureCache = new HashMap<>();

        if (meshes == null) {
            return out;
        }

        int vertexBase = 0;
        for (int meshIndex = 0; meshIndex < scene.mNumMeshes(); meshIndex++) {
            AIMesh aiMesh = AIMesh.create(meshes.get(meshIndex));
            int materialIndex = aiMesh.mMaterialIndex();
            String materialName = "material_" + materialIndex;

            if (isSkippedMaterial(materialIndex)) {
                System.out.println("[AssimpLoader] skipped " + materialName
                        + " because Gura GLB uses it as a black outline shell.");
                vertexBase += aiMesh.mNumVertices();
                continue;
            }

            for (int faceIndex = 0; faceIndex < aiMesh.mNumFaces(); faceIndex++) {
                AIFace face = aiMesh.mFaces().get(faceIndex);
                if (face.mNumIndices() != 3) {
                    continue;
                }

                IntBuffer indices = face.mIndices();
                Vector3[] verts = new Vector3[3];
                Vector3[] uvs = new Vector3[3];
                Vector3[] normals = new Vector3[3];
                int[] vertexIndices = new int[3];

                for (int i = 0; i < 3; i++) {
                    int index = indices.get(i);
                    verts[i] = readPosition(aiMesh, index);
                    uvs[i] = readUv(aiMesh, index);
                    normals[i] = readNormal(aiMesh, index, null);
                    vertexIndices[i] = vertexBase + index;
                }

                Vector3 faceNormal = Vector3.cross(
                        verts[1].sub(verts[0]),
                        verts[2].sub(verts[0])
                ).unit_vector();

                for (int i = 0; i < 3; i++) {
                    if (normals[i] == null) {
                        normals[i] = faceNormal;
                    }
                }

                out.addTriangle(materialName, new Triangle(verts, uvs, normals, vertexIndices));
            }

            vertexBase += aiMesh.mNumVertices();

            Texture texture = textureCache.computeIfAbsent(
                    materialIndex,
                    index -> loadMaterialTexture(scene, index)
            );
            SubMesh subMesh = out.getSubMesh(materialName);
            if (subMesh != null && texture != null && texture.isUploaded()) {
                subMesh.textureKa = texture;
                System.out.println("[AssimpLoader] assigned texture to " + materialName
                        + " (" + texture.getWidth() + "x" + texture.getHeight() + ")");
            }
        }

        System.out.println("[AssimpLoader] converted subMeshes = " + out.getAllSubMeshes().size());
        out.printSubMeshInfo();
        return out;
    }

    private boolean isSkippedMaterial(int materialIndex) {
        return materialIndex == 1;
    }

    private Texture loadMaterialTexture(AIScene scene, int materialIndex) {
        PointerBuffer materials = scene.mMaterials();
        if (materials == null || materialIndex < 0 || materialIndex >= scene.mNumMaterials()) {
            return null;
        }

        AIMaterial material = AIMaterial.create(materials.get(materialIndex));
        AIString texturePath = AIString.calloc();

        try {
            int result = aiGetMaterialTexture(
                    material,
                    aiTextureType_DIFFUSE,
                    0,
                    texturePath,
                    (IntBuffer) null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (result != aiReturn_SUCCESS) {
                result = aiGetMaterialTexture(
                        material,
                        aiTextureType_BASE_COLOR,
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

            if (result != aiReturn_SUCCESS) {
                System.out.println("[AssimpLoader] no diffuse/baseColor texture for material_" + materialIndex);
                return null;
            }

            String path = texturePath.dataString();
            System.out.println("[AssimpLoader] material_" + materialIndex + " texture ref = " + path);

            if (path.startsWith("*")) {
                return loadEmbeddedTexture(scene, path);
            }

            return new Texture(path, true, true);
        } finally {
            texturePath.free();
        }
    }

    private Texture loadEmbeddedTexture(AIScene scene, String ref) {
        PointerBuffer textures = scene.mTextures();
        if (textures == null) {
            return null;
        }

        int textureIndex = parseEmbeddedTextureIndex(ref);
        if (textureIndex < 0 || textureIndex >= scene.mNumTextures()) {
            System.out.println("[AssimpLoader] invalid embedded texture ref: " + ref);
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

    private Vector3 readPosition(AIMesh mesh, int index) {
        var value = mesh.mVertices().get(index);
        return new Vector3(value.x(), value.y(), value.z());
    }

    private Vector3 readNormal(AIMesh mesh, int index, Vector3 fallback) {
        if (mesh.mNormals() == null) {
            return fallback;
        }

        var value = mesh.mNormals().get(index);
        return new Vector3(value.x(), value.y(), value.z());
    }

    private Vector3 readUv(AIMesh mesh, int index) {
        if (mesh.mTextureCoords(0) == null) {
            return new Vector3(0, 0, 0);
        }

        var value = mesh.mTextureCoords(0).get(index);
        return new Vector3(value.x(), value.y(), 0);
    }

    private void printSceneInfo(String path, AIScene scene) {
        System.out.println("[AssimpLoader] loaded: " + path);
        System.out.println("[AssimpLoader] meshes = " + scene.mNumMeshes()
                + ", materials = " + scene.mNumMaterials()
                + ", animations = " + scene.mNumAnimations());

        LinkedHashSet<String> boneNames = new LinkedHashSet<>();
        PointerBuffer meshes = scene.mMeshes();
        if (meshes != null) {
            for (int i = 0; i < scene.mNumMeshes(); i++) {
                AIMesh mesh = AIMesh.create(meshes.get(i));
                PointerBuffer bones = mesh.mBones();
                if (bones == null) {
                    continue;
                }

                for (int j = 0; j < mesh.mNumBones(); j++) {
                    boneNames.add(org.lwjgl.assimp.AIBone.create(bones.get(j)).mName().dataString());
                }
            }
        }
        System.out.println("[AssimpLoader] unique bones = " + boneNames.size());

        PointerBuffer animations = scene.mAnimations();
        if (animations == null) {
            return;
        }

        for (int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation animation = AIAnimation.create(animations.get(i));
            String name = animation.mName().dataString();
            System.out.println("[AssimpLoader] animation[" + i + "] name = " + name
                    + ", duration = " + animation.mDuration()
                    + ", ticksPerSecond = " + animation.mTicksPerSecond()
                    + ", channels = " + animation.mNumChannels());

            PointerBuffer channels = animation.mChannels();
            if (channels == null) {
                continue;
            }

            for (int j = 0; j < Math.min(3, animation.mNumChannels()); j++) {
                AINodeAnim channel = AINodeAnim.create(channels.get(j));
                System.out.println("  channel[" + j + "] node = " + channel.mNodeName().dataString()
                        + ", posKeys = " + channel.mNumPositionKeys()
                        + ", rotKeys = " + channel.mNumRotationKeys()
                        + ", scaleKeys = " + channel.mNumScalingKeys());
            }
        }
    }
}
