package org.example.engine.raytracing;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.Texture;
import org.example.engine.importer.DefaultMeshAssetLoader;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

public class RayTracingSceneBuilder {
    private static final int MAX_DIFFUSE_TEXTURES = 31;
    private static final float DEFAULT_MODEL_TARGET_SIZE = 2.2f;
    private static final int DEFAULT_BVH_DEPTH = 48;
    private static final int DRAGON_MATERIAL_INDEX = 0;
    private static final float DEFAULT_CORNELL_BOX_SIZE = 1.5f;
    private static final boolean ENABLE_DEFAULT_AREA_LIGHT = false;
    private static final float DEFAULT_AREA_LIGHT_Y = 1.2f;
    private static final float DEFAULT_AREA_LIGHT_HALF_SIZE = 1.5f;
    private static final Vector3 SPONZA_AREA_LIGHT_POSITION = new Vector3(0.0f, 4.5f, 0.0f);
    private static final Vector3 SPONZA_AREA_LIGHT_SIZE = new Vector3(2.0f, 1.0f, 10.0f);
    private static final float MIRROR_FLOOR_Y = -4.395f;
    private static final float MIRROR_FLOOR_HALF_SIZE_X = 5.0f;
    private static final float MIRROR_FLOOR_HALF_SIZE_Z = 8.0f;
    private static final int MIRROR_FLOOR_GRID_X = 20;
    private static final int MIRROR_FLOOR_GRID_Z = 32;

    private final RayTracingBvhBuilder bvhBuilder;
    private final RayTracingBvhCache bvhCache;
    private final RayTracingBufferPacker bufferPacker;
    private final CornellBoxFactory cornellBoxFactory;
    private RayTracingMaterialData dragonMaterial = RayTracingMaterialData.dielectric(new Vector3(0.5f), 0.8f, 1.5f);
    private CornellBoxFactory.CornellBoxMaterials cornellBoxMaterials = CornellBoxFactory.CornellBoxMaterials.defaultMaterials();
    private float modelTargetSize = DEFAULT_MODEL_TARGET_SIZE;
    private float cornellBoxSize = DEFAULT_CORNELL_BOX_SIZE;
    private int bvhDepth = DEFAULT_BVH_DEPTH;

    public RayTracingSceneBuilder() {
        bvhBuilder = new RayTracingBvhBuilder();
        bvhCache = new RayTracingBvhCache();
        bufferPacker = new RayTracingBufferPacker();
        cornellBoxFactory = new CornellBoxFactory();
    }

    public RayTracingSceneBuilder setDragonMaterial(RayTracingMaterialData dragonMaterial) {
        if (dragonMaterial != null) {
            this.dragonMaterial = dragonMaterial;
        }
        return this;
    }

    public RayTracingSceneBuilder setCornellBoxMaterials(CornellBoxFactory.CornellBoxMaterials cornellBoxMaterials) {
        if (cornellBoxMaterials != null) {
            this.cornellBoxMaterials = cornellBoxMaterials;
        }
        return this;
    }

    public RayTracingSceneBuilder setModelTargetSize(float modelTargetSize) {
        this.modelTargetSize = Math.max(0.0001f, modelTargetSize);
        return this;
    }

    public RayTracingSceneBuilder setCornellBoxSize(float cornellBoxSize) {
        this.cornellBoxSize = Math.max(0.0001f, cornellBoxSize);
        return this;
    }

    public RayTracingSceneBuilder setBvhDepth(int bvhDepth) {
        this.bvhDepth = Math.max(0, bvhDepth);
        return this;
    }

    public RayTracingSceneBuffers buildDragonCornellScene(String meshPath) {
        ArrayList<RayTracingTriangle> meshTriangles = collectMeshTriangles(meshPath);
        ArrayList<RayTracingMaterialData> materials = defaultMaterials();

        if (meshTriangles.isEmpty()) {
            return new RayTracingSceneBuffers(
                    bufferPacker.createDummyMeshTriangleBuffer(),
                    0,
                    bufferPacker.createDummySphereBuffer(),
                    0,
                    bufferPacker.createDummyNodeBuffer(),
                    bufferPacker.createDummyFullTriangleBuffer(),
                    0,
                    bufferPacker.createMaterialBuffer(materials),
                    materials.size()
            );
        }

        ArrayList<RayTracingTriangle> normalizedTriangles = normalizeTriangles(meshTriangles, DRAGON_MATERIAL_INDEX);
        RayTracingBvhBuilder.BvhBuildResult bvh = bvhBuilder.build(normalizedTriangles, bvhDepth);
        CornellBoxFactory.CornellBox cornellBox = cornellBoxFactory.createOpenFrontBox(cornellBoxSize, cornellBoxMaterials);

        ComputeBuffer triangleBuffer = bufferPacker.createMeshTriangleBuffer(bvh.orderedTriangles);
        ComputeBuffer nodeBuffer = bufferPacker.createNodeBuffer(
                bvh.nodes,
                calculateBounds(normalizedTriangles),
                bvh.orderedTriangles.size()
        );

        System.out.println("[RayTracingSceneBuilder] loaded " + bvh.orderedTriangles.size() + " triangles from " + meshPath);
        return new RayTracingSceneBuffers(
                triangleBuffer,
                bvh.orderedTriangles.size(),
                bufferPacker.createDummySphereBuffer(),
                0,
                nodeBuffer,
                bufferPacker.createFullTriangleBuffer(cornellBox.triangles, cornellBox.materials),
                cornellBox.triangles.size(),
                bufferPacker.createMaterialBuffer(materials),
                materials.size()
        );
    }

    public RayTracingSceneBuffers buildStaticScene(List<RayTracingMeshInstance> instances) {
        ArrayList<RayTracingMaterialData> materials = new ArrayList<>();
        ArrayList<RayTracingTriangle> sceneTriangles = new ArrayList<>();
        ArrayList<Texture> diffuseTextures = new ArrayList<>();
        IdentityHashMap<Texture, Integer> diffuseTextureIndices = new IdentityHashMap<>();
        ArrayList<RayTracingSphereData> sphereLights = createDefaultSphereLights();
        StringBuilder cacheKey = new StringBuilder("static-bvh=sah-stack-safe-v2")
                .append("|depth=").append(bvhDepth)
                .append("|leaf=").append(RayTracingBvhBuilder.MAX_TRIANGLES_PER_LEAF)
                .append("|sahBins=").append(RayTracingBvhBuilder.SAH_BIN_COUNT);

        List<RayTracingMeshInstance> safeInstances = instances == null ? Collections.emptyList() : instances;
        for (RayTracingMeshInstance instance : safeInstances) {
            if (instance == null || instance.meshPath == null || instance.meshPath.isEmpty()) {
                continue;
            }

            Mesh mesh = new DefaultMeshAssetLoader().load(instance.meshPath).getMesh();
            ArrayList<RayTracingTriangle> meshTriangles = collectMeshTriangles(
                    mesh,
                    instance.material,
                    materials,
                    diffuseTextures,
                    diffuseTextureIndices
            );
            if (meshTriangles.isEmpty()) {
                continue;
            }

            appendInstanceCacheKey(cacheKey, instance, materials.size());
            if (instance.normalizeToTargetSize) {
                meshTriangles = normalizeTriangles(meshTriangles, instance.targetSize);
            }

            sceneTriangles.addAll(transformTriangles(meshTriangles, instance.transform));
            System.out.println("[RayTracingSceneBuilder] loaded " + meshTriangles.size() + " triangles from " + instance.meshPath);
        }

        if (materials.isEmpty()) {
            materials.add(RayTracingMaterialData.lambertian(new Vector3(0.0f)));
        }

        if (ENABLE_DEFAULT_AREA_LIGHT) {
            addDefaultAreaLight(sceneTriangles, materials);
        }
        addMirrorCheckerFloor(sceneTriangles, materials);
        addSponzaAreaLight(sceneTriangles, materials);
        cacheKey.append("|defaultAreaLight=").append(ENABLE_DEFAULT_AREA_LIGHT)
                .append("|defaultAreaLightY=").append(DEFAULT_AREA_LIGHT_Y)
                .append("|defaultAreaLightHalfSize=").append(DEFAULT_AREA_LIGHT_HALF_SIZE)
                .append("|mirrorCheckerFloorY=").append(MIRROR_FLOOR_Y)
                .append("|mirrorCheckerFloorSize=")
                .append(MIRROR_FLOOR_HALF_SIZE_X).append(',')
                .append(MIRROR_FLOOR_HALF_SIZE_Z)
                .append("|mirrorCheckerFloorGrid=")
                .append(MIRROR_FLOOR_GRID_X).append(',')
                .append(MIRROR_FLOOR_GRID_Z)
                .append("|sponzaAreaLightVisible=true")
                .append("|sponzaAreaLightPos=")
                .append(SPONZA_AREA_LIGHT_POSITION.x).append(',')
                .append(SPONZA_AREA_LIGHT_POSITION.y).append(',')
                .append(SPONZA_AREA_LIGHT_POSITION.z)
                .append("|sponzaAreaLightSize=")
                .append(SPONZA_AREA_LIGHT_SIZE.x).append(',')
                .append(SPONZA_AREA_LIGHT_SIZE.y).append(',')
                .append(SPONZA_AREA_LIGHT_SIZE.z);

        if (sceneTriangles.isEmpty()) {
            return new RayTracingSceneBuffers(
                    bufferPacker.createDummyMeshTriangleBuffer(),
                    0,
                    bufferPacker.createSphereBuffer(sphereLights),
                    sphereLights.size(),
                    bufferPacker.createDummyNodeBuffer(),
                    bufferPacker.createDummyFullTriangleBuffer(),
                    0,
                    bufferPacker.createMaterialBuffer(materials),
                    materials.size(),
                    diffuseTextures
            );
        }

        RayTracingBounds sceneBounds = calculateBounds(sceneTriangles);
        RayTracingBvhCache.CachedBvh cachedBvh = bvhCache.getOrBuild(cacheKey.toString(), sceneTriangles, bvhDepth, sceneBounds);
        RayTracingBvhBuilder.BvhBuildResult bvh = cachedBvh.bvh;
        ComputeBuffer triangleBuffer = bufferPacker.createMeshTriangleBuffer(bvh.orderedTriangles);
        ComputeBuffer nodeBuffer = bufferPacker.createNodeBuffer(
                bvh.nodes,
                cachedBvh.bounds,
                bvh.orderedTriangles.size()
        );

        System.out.println("[RayTracingSceneBuilder] loaded " + bvh.orderedTriangles.size() + " static scene triangles");
        return new RayTracingSceneBuffers(
                triangleBuffer,
                bvh.orderedTriangles.size(),
                bufferPacker.createSphereBuffer(sphereLights),
                sphereLights.size(),
                nodeBuffer,
                bufferPacker.createDummyFullTriangleBuffer(),
                0,
                bufferPacker.createMaterialBuffer(materials),
                materials.size(),
                diffuseTextures
        );
    }

    private ArrayList<RayTracingMaterialData> defaultMaterials() {
        ArrayList<RayTracingMaterialData> materials = new ArrayList<>();
        materials.add(dragonMaterial);
        return materials;
    }

    private ArrayList<RayTracingTriangle> collectMeshTriangles(String meshPath) {
        Mesh mesh = new DefaultMeshAssetLoader().load(meshPath).getMesh();
        ArrayList<RayTracingTriangle> triangles = new ArrayList<>();
        if (mesh == null) {
            return triangles;
        }

        for (SubMesh subMesh : mesh.getAllSubMeshes()) {
            float[] positions = subMesh.positions;
            int[] indices = subMesh.indices;
            if (positions == null || indices == null) {
                continue;
            }

            for (int i = 0; i + 2 < indices.length; i += 3) {
                triangles.add(new RayTracingTriangle(
                        readPosition(positions, indices[i]),
                        readPosition(positions, indices[i + 1]),
                        readPosition(positions, indices[i + 2])
                ));
            }
        }

        return triangles;
    }

    private ArrayList<RayTracingTriangle> collectMeshTriangles(
            Mesh mesh,
            RayTracingMaterialData baseMaterial,
            ArrayList<RayTracingMaterialData> materials,
            ArrayList<Texture> diffuseTextures,
            IdentityHashMap<Texture, Integer> diffuseTextureIndices
    ) {
        ArrayList<RayTracingTriangle> triangles = new ArrayList<>();
        if (mesh == null) {
            return triangles;
        }

        for (SubMesh subMesh : mesh.getAllSubMeshes()) {
            float[] positions = subMesh.positions;
            float[] uvs = subMesh.uvs;
            int[] indices = subMesh.indices;
            if (positions == null || indices == null) {
                continue;
            }

            int textureIndex = registerDiffuseTexture(subMesh.textureKa, diffuseTextures, diffuseTextureIndices);
            int materialIndex = materials.size();
            materials.add(baseMaterial.withTextureIndex(textureIndex));

            for (int i = 0; i + 2 < indices.length; i += 3) {
                triangles.add(new RayTracingTriangle(
                        readPosition(positions, indices[i]),
                        readPosition(positions, indices[i + 1]),
                        readPosition(positions, indices[i + 2]),
                        readUv(uvs, indices[i]),
                        readUv(uvs, indices[i + 1]),
                        readUv(uvs, indices[i + 2]),
                        materialIndex
                ));
            }
        }

        return triangles;
    }

    private int registerDiffuseTexture(
            Texture texture,
            ArrayList<Texture> diffuseTextures,
            IdentityHashMap<Texture, Integer> diffuseTextureIndices
    ) {
        if (texture == null || !texture.isUploaded()) {
            return -1;
        }

        Integer existing = diffuseTextureIndices.get(texture);
        if (existing != null) {
            return existing;
        }

        if (diffuseTextures.size() >= MAX_DIFFUSE_TEXTURES) {
            System.out.println("[RayTracingSceneBuilder] diffuse texture limit reached; fallback to material albedo");
            return -1;
        }

        int index = diffuseTextures.size();
        diffuseTextures.add(texture);
        diffuseTextureIndices.put(texture, index);
        return index;
    }

    private Vector3 readPosition(float[] positions, int index) {
        int base = index * 3;
        if (base < 0 || base + 2 >= positions.length) {
            return new Vector3(0.0f);
        }
        return new Vector3(positions[base], positions[base + 1], positions[base + 2]);
    }

    private Vector3 readUv(float[] uvs, int index) {
        int base = index * 2;
        if (uvs == null || base < 0 || base + 1 >= uvs.length) {
            return new Vector3(0.0f);
        }
        return new Vector3(uvs[base], uvs[base + 1], 0.0f);
    }

    private ArrayList<RayTracingTriangle> normalizeTriangles(List<RayTracingTriangle> triangles, int materialIndex) {
        return normalizeTriangles(triangles, materialIndex, modelTargetSize);
    }

    private ArrayList<RayTracingTriangle> normalizeTriangles(List<RayTracingTriangle> triangles, int materialIndex, float targetSize) {
        ArrayList<RayTracingTriangle> normalized = normalizeTriangles(triangles, targetSize);
        ArrayList<RayTracingTriangle> remapped = new ArrayList<>(normalized.size());
        for (RayTracingTriangle triangle : normalized) {
            remapped.add(triangle.withMaterialIndex(materialIndex));
        }
        return remapped;
    }

    private ArrayList<RayTracingTriangle> normalizeTriangles(List<RayTracingTriangle> triangles, float targetSize) {
        RayTracingBounds bounds = calculateBounds(triangles);
        Vector3 boundsSize = bounds.size();
        float maxSize = Math.max(boundsSize.x, Math.max(boundsSize.y, boundsSize.z));
        float scale = targetSize / Math.max(maxSize, 0.0001f);
        Vector3 center = bounds.center();

        ArrayList<RayTracingTriangle> normalized = new ArrayList<>(triangles.size());
        for (RayTracingTriangle triangle : triangles) {
            normalized.add(new RayTracingTriangle(
                    triangle.p0.sub(center).mult(scale),
                    triangle.p1.sub(center).mult(scale),
                    triangle.p2.sub(center).mult(scale),
                    triangle.uv0,
                    triangle.uv1,
                    triangle.uv2,
                    triangle.materialIndex
            ));
        }
        return normalized;
    }

    private ArrayList<RayTracingTriangle> transformTriangles(
            List<RayTracingTriangle> triangles,
            Matrix4 transform,
            int materialIndex
    ) {
        ArrayList<RayTracingTriangle> transformed = transformTriangles(triangles, transform);
        ArrayList<RayTracingTriangle> remapped = new ArrayList<>(transformed.size());
        for (RayTracingTriangle triangle : transformed) {
            remapped.add(triangle.withMaterialIndex(materialIndex));
        }
        return remapped;
    }

    private ArrayList<RayTracingTriangle> transformTriangles(
            List<RayTracingTriangle> triangles,
            Matrix4 transform
    ) {
        Matrix4 safeTransform = transform == null ? Matrix4.Identity() : transform;
        ArrayList<RayTracingTriangle> transformed = new ArrayList<>(triangles.size());
        for (RayTracingTriangle triangle : triangles) {
            transformed.add(new RayTracingTriangle(
                    safeTransform.transformPoint(triangle.p0),
                    safeTransform.transformPoint(triangle.p1),
                    safeTransform.transformPoint(triangle.p2),
                    triangle.uv0,
                    triangle.uv1,
                    triangle.uv2,
                    triangle.materialIndex
            ));
        }
        return transformed;
    }

    private void addDefaultAreaLight(ArrayList<RayTracingTriangle> sceneTriangles, ArrayList<RayTracingMaterialData> materials) {
        int materialIndex = materials.size();
        materials.add(RayTracingMaterialData.emissive(new Vector3(6.0f)));

        float y = DEFAULT_AREA_LIGHT_Y;
        float s = DEFAULT_AREA_LIGHT_HALF_SIZE;
        Vector3 p0 = new Vector3(-s, y, -s);
        Vector3 p1 = new Vector3(s, y, -s);
        Vector3 p2 = new Vector3(s, y, s);
        Vector3 p3 = new Vector3(-s, y, s);

        sceneTriangles.add(new RayTracingTriangle(p0, p1, p2, materialIndex));
        sceneTriangles.add(new RayTracingTriangle(p0, p2, p3, materialIndex));
    }

    private void addSponzaAreaLight(ArrayList<RayTracingTriangle> sceneTriangles, ArrayList<RayTracingMaterialData> materials) {
        int materialIndex = materials.size();
        materials.add(RayTracingMaterialData.emissive(new Vector3(8.0f, 6.4f, 4.2f)));

        float halfX = SPONZA_AREA_LIGHT_SIZE.x * 0.5f;
        float halfZ = SPONZA_AREA_LIGHT_SIZE.z * 0.5f;
        Vector3 center = SPONZA_AREA_LIGHT_POSITION;

        Vector3 p0 = new Vector3(center.x - halfX, center.y, center.z - halfZ);
        Vector3 p1 = new Vector3(center.x + halfX, center.y, center.z - halfZ);
        Vector3 p2 = new Vector3(center.x + halfX, center.y, center.z + halfZ);
        Vector3 p3 = new Vector3(center.x - halfX, center.y, center.z + halfZ);

        sceneTriangles.add(new RayTracingTriangle(p0, p1, p2, materialIndex));
        sceneTriangles.add(new RayTracingTriangle(p0, p2, p3, materialIndex));
    }

    private void addMirrorCheckerFloor(ArrayList<RayTracingTriangle> sceneTriangles, ArrayList<RayTracingMaterialData> materials) {
        int whiteMaterialIndex = materials.size();
        materials.add(RayTracingMaterialData.metal(new Vector3(0.98f, 0.98f, 0.98f), 0.04f));
        int blackMaterialIndex = materials.size();
        materials.add(RayTracingMaterialData.metal(new Vector3(0.42f, 0.42f, 0.42f), 0.04f));

        float minX = -MIRROR_FLOOR_HALF_SIZE_X;
        float minZ = -MIRROR_FLOOR_HALF_SIZE_Z;
        float cellSizeX = (MIRROR_FLOOR_HALF_SIZE_X * 2.0f) / MIRROR_FLOOR_GRID_X;
        float cellSizeZ = (MIRROR_FLOOR_HALF_SIZE_Z * 2.0f) / MIRROR_FLOOR_GRID_Z;

        for (int z = 0; z < MIRROR_FLOOR_GRID_Z; z++) {
            for (int x = 0; x < MIRROR_FLOOR_GRID_X; x++) {
                float x0 = minX + cellSizeX * x;
                float x1 = x0 + cellSizeX;
                float z0 = minZ + cellSizeZ * z;
                float z1 = z0 + cellSizeZ;
                int materialIndex = ((x + z) & 1) == 0 ? whiteMaterialIndex : blackMaterialIndex;
                addFloorQuad(sceneTriangles, x0, x1, z0, z1, materialIndex);
            }
        }
    }

    private void addFloorQuad(
            ArrayList<RayTracingTriangle> sceneTriangles,
            float x0,
            float x1,
            float z0,
            float z1,
            int materialIndex
    ) {
        Vector3 p0 = new Vector3(x0, MIRROR_FLOOR_Y, z0);
        Vector3 p1 = new Vector3(x1, MIRROR_FLOOR_Y, z0);
        Vector3 p2 = new Vector3(x1, MIRROR_FLOOR_Y, z1);
        Vector3 p3 = new Vector3(x0, MIRROR_FLOOR_Y, z1);
        sceneTriangles.add(new RayTracingTriangle(p0, p1, p2, materialIndex));
        sceneTriangles.add(new RayTracingTriangle(p0, p2, p3, materialIndex));
    }

    private ArrayList<RayTracingSphereData> createDefaultSphereLights() {
        ArrayList<RayTracingSphereData> lights = new ArrayList<>();
        float lightNum = 6;
        float startZ = 6.1f;
        float endZ = -6.1f;
        float stepZ = (endZ - startZ) / (lightNum - 1);
        for (int i = 0; i < lightNum ; i++) {
            float z = startZ + stepZ * i;
            addSphereLight(lights, new Vector3(-1.3f, -3.0f, z), 0.12f);
            addSphereLight(lights, new Vector3(1.3f, -3.0f, z), 0.12f);
        }
        addSphereLight(lights, new Vector3(-3.0f, -3.5f, -7.7f),0.3f);
        addSphereLight(lights, new Vector3(3.0f, -3.5f, -7.7f),0.3f);
        return lights;
    }

    private void addSphereLight(ArrayList<RayTracingSphereData> lights, Vector3 position, float radius) {
        lights.add(new RayTracingSphereData(
                position,
                radius,
                RayTracingMaterialData.emissive(new Vector3(7.0f * 3f, 5.7f * 3f, 3.8f * 3f))
        ));
    }

    private void appendInstanceCacheKey(StringBuilder cacheKey, RayTracingMeshInstance instance, int materialIndex) {
        cacheKey.append("|matIndex=").append(materialIndex)
                .append("|path=").append(instance.meshPath)
                .append("|normalize=").append(instance.normalizeToTargetSize)
                .append("|target=").append(instance.targetSize)
                .append("|m=");
        for (float value : instance.transform.m) {
            cacheKey.append(value).append(',');
        }
    }

    private RayTracingBounds calculateBounds(List<RayTracingTriangle> triangles) {
        RayTracingBounds bounds = new RayTracingBounds();
        for (RayTracingTriangle triangle : triangles) {
            bounds.include(triangle);
        }
        return bounds;
    }
}
