package org.example.engine.prt;

import org.example.engine.math.SphereHarmonic;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;

import java.util.ArrayList;

public class TransferBaker {

    public static final int DEFAULT_SAMPLE_COUNT = 512;

    private final TransferCache cache = new TransferCache();

    public ArrayList<TransferData> loadOrBake(String meshResourcePath, Mesh mesh) {
        return loadOrBake(meshResourcePath, mesh, SHCoefficients.DEFAULT_BANDS, DEFAULT_SAMPLE_COUNT, PRTBakeMode.UNSHADOW);
    }

    public ArrayList<TransferData> loadOrBake(String meshResourcePath, Mesh mesh, int bands, int sampleCount) {
        return loadOrBake(meshResourcePath, mesh, bands, sampleCount, PRTBakeMode.UNSHADOW);
    }

    public ArrayList<TransferData> loadOrBake(
            String meshResourcePath,
            Mesh mesh,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode
    ) {
        mesh.finishBuild();
        PRTBakeMode mode = bakeMode == null ? PRTBakeMode.UNSHADOW : bakeMode;

        if (cache.exists(meshResourcePath, bands, sampleCount, mode)) {
            ArrayList<TransferData> cached = cache.load(meshResourcePath, mesh, bands, sampleCount, mode);
            applyToMesh(mesh, cached);
            System.out.println("[TransferBaker] Loaded PRT transfer cache: "
                    + cache.cachePath(meshResourcePath, bands, sampleCount, mode));
            return cached;
        }

        ArrayList<TransferData> baked = bake(mesh, bands, sampleCount, mode);
        cache.save(meshResourcePath, mesh, baked);
        applyToMesh(mesh, baked);
        System.out.println("[TransferBaker] Baked PRT transfer cache: "
                + cache.cachePath(meshResourcePath, bands, sampleCount, mode));
        return baked;
    }

    public ArrayList<TransferData> bake(Mesh mesh, int bands, int sampleCount) {
        return bake(mesh, bands, sampleCount, PRTBakeMode.UNSHADOW);
    }

    public ArrayList<TransferData> bake(Mesh mesh, int bands, int sampleCount, PRTBakeMode bakeMode) {
        if (sampleCount <= 0) {
            throw new IllegalArgumentException("[TransferBaker] sampleCount must be positive.");
        }

        PRTBakeMode mode = bakeMode == null ? PRTBakeMode.UNSHADOW : bakeMode;
        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        ArrayList<TransferData> out = new ArrayList<>();
        DirectionSample[] samples = buildSphereSamples(sampleCount, bands);
        TriangleOccluder occluder = mode == PRTBakeMode.SHADOW ? new TriangleOccluder(subMeshes) : null;
        TransferWeightFunction weightFunction = selectWeightFunction(mode, occluder);
        BakeProgress progress = new BakeProgress(totalVertexCount(subMeshes));

        System.out.println("[TransferBaker] Baking PRT transfer: mode=" + mode
                + ", bands=" + bands
                + ", samples=" + sampleCount
                + ", vertices=" + progress.totalVertices);

        for (SubMesh subMesh : subMeshes) {
            out.add(bakeSubMesh(subMesh, bands, sampleCount, mode, samples, weightFunction, progress));
        }

        progress.finish();
        return out;
    }

    private TransferData bakeSubMesh(
            SubMesh subMesh,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            DirectionSample[] samples,
            TransferWeightFunction weightFunction,
            BakeProgress progress
    ) {
        int vertexCount = subMesh.getVertexCount();
        TransferData data = new TransferData(vertexCount, bands, sampleCount, bakeMode);
        int coefficientCount = data.getCoefficientCount();
        float integralScale = 4.0f * (float) Math.PI / sampleCount;

        for (int v = 0; v < vertexCount; v++) {
            Vector3 position = readPosition(subMesh, v);
            Vector3 normal = readNormal(subMesh, v);

            for (DirectionSample sample : samples) {
                float transferWeight = weightFunction.compute(new TransferSampleContext(
                        position,
                        normal,
                        sample.direction
                ));
                if (transferWeight <= 0.0f) {
                    continue;
                }

                float weight = transferWeight * integralScale;
                for (int k = 0; k < coefficientCount; k++) {
                    data.set(v, k, data.get(v, k) + sample.sh[k] * weight);
                }
            }

            progress.vertexDone();
        }

        return data;
    }

    private TransferWeightFunction selectWeightFunction(PRTBakeMode mode, TriangleOccluder occluder) {
        return switch (mode) {
            case UNSHADOW -> this::computeUnshadowWeight;
            case SHADOW -> context -> computeShadowWeight(context, occluder);
        };
    }

    private float computeUnshadowWeight(TransferSampleContext context) {
        return Math.max(0.0f, Vector3.dot(context.normal, context.direction));
    }

    private float computeShadowWeight(TransferSampleContext context, TriangleOccluder occluder) {
        float ndotl = computeUnshadowWeight(context);
        if (ndotl <= 0.0f) {
            return 0.0f;
        }

        if (occluder == null || occluder.isOccluded(context.position, context.normal, context.direction)) {
            return 0.0f;
        }

        return ndotl;
    }

    private int totalVertexCount(ArrayList<SubMesh> subMeshes) {
        int total = 0;
        for (SubMesh subMesh : subMeshes) {
            if (subMesh != null) {
                total += subMesh.getVertexCount();
            }
        }
        return total;
    }

    private DirectionSample[] buildSphereSamples(int sampleCount, int bands) {
        DirectionSample[] samples = new DirectionSample[sampleCount];
        float goldenAngle = (float) (Math.PI * (3.0 - Math.sqrt(5.0)));

        for (int i = 0; i < sampleCount; i++) {
            float z = 1.0f - 2.0f * (i + 0.5f) / sampleCount;
            float radius = (float) Math.sqrt(Math.max(0.0f, 1.0f - z * z));
            float phi = i * goldenAngle;
            Vector3 direction = new Vector3(
                    radius * (float) Math.cos(phi),
                    radius * (float) Math.sin(phi),
                    z
            );
            samples[i] = new DirectionSample(direction, bands);
        }

        return samples;
    }

    private Vector3 readPosition(SubMesh subMesh, int vertexIndex) {
        int base = vertexIndex * 3;
        if (subMesh.positions == null || base + 2 >= subMesh.positions.length) {
            return Vector3.Zero();
        }

        return new Vector3(
                subMesh.positions[base],
                subMesh.positions[base + 1],
                subMesh.positions[base + 2]
        );
    }

    private Vector3 readNormal(SubMesh subMesh, int vertexIndex) {
        int base = vertexIndex * 3;
        if (subMesh.normals == null || base + 2 >= subMesh.normals.length) {
            return new Vector3(0.0f, 1.0f, 0.0f);
        }

        Vector3 normal = new Vector3(
                subMesh.normals[base],
                subMesh.normals[base + 1],
                subMesh.normals[base + 2]
        ).unit_vector();

        if (normal.length_squared() < 1e-8f) {
            return new Vector3(0.0f, 1.0f, 0.0f);
        }

        return normal;
    }

    private void applyToMesh(Mesh mesh, ArrayList<TransferData> transferData) {
        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        for (int i = 0; i < subMeshes.size() && i < transferData.size(); i++) {
            SubMesh subMesh = subMeshes.get(i);
            TransferData data = transferData.get(i);
            subMesh.prtBands = data.getBands();
            subMesh.prtSampleCount = data.getSampleCount();
            subMesh.prtBakeMode = data.getBakeMode();
            subMesh.prtCoefficients = data.raw();
        }
    }

    private static class DirectionSample {
        final Vector3 direction;
        final float[] sh;

        DirectionSample(Vector3 direction, int bands) {
            this.direction = direction;
            this.sh = new float[bands * bands];

            int index = 0;
            for (int l = 0; l < bands; l++) {
                for (int m = -l; m <= l; m++) {
                    sh[index++] = SphereHarmonic.EvalSH(l, m, direction);
                }
            }
        }
    }

    @FunctionalInterface
    private interface TransferWeightFunction {
        float compute(TransferSampleContext context);
    }

    private static class TransferSampleContext {
        final Vector3 position;
        final Vector3 normal;
        final Vector3 direction;

        TransferSampleContext(Vector3 position, Vector3 normal, Vector3 direction) {
            this.position = position;
            this.normal = normal;
            this.direction = direction;
        }
    }

    private static class TriangleOccluder {
        private static final float ORIGIN_BIAS = 1e-4f;
        private static final float RAY_EPSILON = 1e-5f;
        private static final int MAX_BVH_DEPTH = 32;
        private static final int LEAF_TRIANGLE_COUNT = 8;

        final ArrayList<Triangle> triangles = new ArrayList<>();
        BVHNode root;

        TriangleOccluder(ArrayList<SubMesh> subMeshes) {
            for (SubMesh subMesh : subMeshes) {
                collectTriangles(subMesh);
            }
            if (!triangles.isEmpty()) {
                root = new BVHNode(triangles, 0, MAX_BVH_DEPTH, LEAF_TRIANGLE_COUNT);
            }
            System.out.println("[TransferBaker] Shadow occluder triangles=" + triangles.size()
                    + ", bvh=" + (root == null ? "empty" : "ready"));
        }

        boolean isOccluded(Vector3 position, Vector3 normal, Vector3 direction) {
            if (root == null) {
                return false;
            }

            Vector3 origin = position.add(normal.mult(ORIGIN_BIAS));
            return root.intersects(origin, direction, RAY_EPSILON, Float.POSITIVE_INFINITY);
        }

        private void collectTriangles(SubMesh subMesh) {
            if (subMesh.positions == null || subMesh.indices == null) {
                return;
            }

            int vertexCount = subMesh.getVertexCount();
            for (int i = 0; i + 2 < subMesh.indices.length; i += 3) {
                int i0 = subMesh.indices[i];
                int i1 = subMesh.indices[i + 1];
                int i2 = subMesh.indices[i + 2];
                if (!validIndex(i0, vertexCount) || !validIndex(i1, vertexCount) || !validIndex(i2, vertexCount)) {
                    continue;
                }

                triangles.add(new Triangle(
                        readVertex(subMesh, i0),
                        readVertex(subMesh, i1),
                        readVertex(subMesh, i2)
                ));
            }
        }

        private static boolean intersectTriangle(Vector3 origin, Vector3 direction, Triangle triangle, float tMin, float tMax) {
            Vector3 edge1 = triangle.v1.sub(triangle.v0);
            Vector3 edge2 = triangle.v2.sub(triangle.v0);
            Vector3 pvec = Vector3.cross(direction, edge2);
            float det = Vector3.dot(edge1, pvec);

            if (Math.abs(det) < RAY_EPSILON) {
                return false;
            }

            float invDet = 1.0f / det;
            Vector3 tvec = origin.sub(triangle.v0);
            float u = Vector3.dot(tvec, pvec) * invDet;
            if (u < 0.0f || u > 1.0f) {
                return false;
            }

            Vector3 qvec = Vector3.cross(tvec, edge1);
            float v = Vector3.dot(direction, qvec) * invDet;
            if (v < 0.0f || u + v > 1.0f) {
                return false;
            }

            float t = Vector3.dot(edge2, qvec) * invDet;
            return t > tMin && t < tMax;
        }

        private static boolean validIndex(int index, int vertexCount) {
            return index >= 0 && index < vertexCount;
        }

        private static Vector3 readVertex(SubMesh subMesh, int vertexIndex) {
            int base = vertexIndex * 3;
            return new Vector3(
                    subMesh.positions[base],
                    subMesh.positions[base + 1],
                    subMesh.positions[base + 2]
            );
        }

        private static class BVHNode {
            final AABB bounds;
            final BVHNode childA;
            final BVHNode childB;
            final ArrayList<Triangle> leafTriangles;

            BVHNode(ArrayList<Triangle> input, int depth, int maxDepth, int leafTriangleCount) {
                bounds = AABB.fromTriangles(input);

                if (depth >= maxDepth || input.size() <= leafTriangleCount) {
                    childA = null;
                    childB = null;
                    leafTriangles = input;
                    return;
                }

                int axis = bounds.longestAxis();
                float split = bounds.center(axis);
                ArrayList<Triangle> triA = new ArrayList<>();
                ArrayList<Triangle> triB = new ArrayList<>();

                for (Triangle triangle : input) {
                    if (triangle.center.xyz(axis) < split) {
                        triA.add(triangle);
                    } else {
                        triB.add(triangle);
                    }
                }

                if (triA.isEmpty() || triB.isEmpty()) {
                    childA = null;
                    childB = null;
                    leafTriangles = input;
                    return;
                }

                childA = new BVHNode(triA, depth + 1, maxDepth, leafTriangleCount);
                childB = new BVHNode(triB, depth + 1, maxDepth, leafTriangleCount);
                leafTriangles = null;
            }

            boolean intersects(Vector3 origin, Vector3 direction, float tMin, float tMax) {
                if (!bounds.intersects(origin, direction, tMin, tMax)) {
                    return false;
                }

                if (leafTriangles != null) {
                    for (Triangle triangle : leafTriangles) {
                        if (intersectTriangle(origin, direction, triangle, tMin, tMax)) {
                            return true;
                        }
                    }
                    return false;
                }

                return childA.intersects(origin, direction, tMin, tMax)
                        || childB.intersects(origin, direction, tMin, tMax);
            }
        }

        private static class AABB {
            final Vector3 min;
            final Vector3 max;

            AABB(Vector3 min, Vector3 max) {
                this.min = min;
                this.max = max;
            }

            static AABB fromTriangles(ArrayList<Triangle> triangles) {
                Vector3 min = new Vector3(Float.POSITIVE_INFINITY);
                Vector3 max = new Vector3(Float.NEGATIVE_INFINITY);

                for (Triangle triangle : triangles) {
                    include(min, max, triangle.v0);
                    include(min, max, triangle.v1);
                    include(min, max, triangle.v2);
                }

                return new AABB(min, max);
            }

            boolean intersects(Vector3 origin, Vector3 direction, float tMin, float tMax) {
                for (int axis = 0; axis < 3; axis++) {
                    float dir = direction.xyz(axis);
                    float originValue = origin.xyz(axis);
                    float minValue = min.xyz(axis);
                    float maxValue = max.xyz(axis);

                    if (Math.abs(dir) < RAY_EPSILON) {
                        if (originValue < minValue || originValue > maxValue) {
                            return false;
                        }
                        continue;
                    }

                    float invDir = 1.0f / dir;
                    float t0 = (minValue - originValue) * invDir;
                    float t1 = (maxValue - originValue) * invDir;
                    if (invDir < 0.0f) {
                        float temp = t0;
                        t0 = t1;
                        t1 = temp;
                    }

                    tMin = Math.max(tMin, t0);
                    tMax = Math.min(tMax, t1);
                    if (tMax <= tMin) {
                        return false;
                    }
                }

                return true;
            }

            int longestAxis() {
                Vector3 size = max.sub(min);
                if (size.x > size.y && size.x > size.z) {
                    return 0;
                }
                return size.y > size.z ? 1 : 2;
            }

            float center(int axis) {
                return (min.xyz(axis) + max.xyz(axis)) * 0.5f;
            }

            private static void include(Vector3 min, Vector3 max, Vector3 point) {
                min.set(
                        Math.min(min.x, point.x),
                        Math.min(min.y, point.y),
                        Math.min(min.z, point.z)
                );
                max.set(
                        Math.max(max.x, point.x),
                        Math.max(max.y, point.y),
                        Math.max(max.z, point.z)
                );
            }
        }
    }

    private static class Triangle {
        final Vector3 v0;
        final Vector3 v1;
        final Vector3 v2;
        final Vector3 center;

        Triangle(Vector3 v0, Vector3 v1, Vector3 v2) {
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.center = new Vector3(
                    (v0.x + v1.x + v2.x) / 3.0f,
                    (v0.y + v1.y + v2.y) / 3.0f,
                    (v0.z + v1.z + v2.z) / 3.0f
            );
        }
    }

    private static class BakeProgress {
        final int totalVertices;
        int bakedVertices = 0;
        int lastPrintedPercent = -1;

        BakeProgress(int totalVertices) {
            this.totalVertices = Math.max(0, totalVertices);
            print(0);
        }

        void vertexDone() {
            bakedVertices++;

            if (totalVertices <= 0) {
                return;
            }

            int percent = Math.min(100, (int) ((long) bakedVertices * 100L / totalVertices));
            if (percent > lastPrintedPercent) {
                print(percent);
            }
        }

        void finish() {
            print(100);
        }

        private void print(int percent) {
            if (percent == lastPrintedPercent) {
                return;
            }

            lastPrintedPercent = percent;
            System.out.println("[TransferBaker] Bake progress: " + percent + "% ("
                    + Math.min(bakedVertices, totalVertices) + "/" + totalVertices + " vertices)");
        }
    }
}
