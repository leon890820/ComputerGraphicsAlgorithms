package org.example.engine.prt;

import org.example.engine.math.SphereHarmonic;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;

import java.util.ArrayList;

public class TransferBaker {

    public static final int DEFAULT_SAMPLE_COUNT = 512;
    public static final int DEFAULT_INTERREFLECTION_BOUNCES = 3;
    private static final float DEFAULT_INTERREFLECTION_ALBEDO = 0.65f;
    private static final Vector3 FIXED_GLOSSY_VIEW_DIRECTION = new Vector3(0.0f, 0.0f, 1.0f);
    public static final Vector3 DEFAULT_FIXED_GLOSSY_CAMERA_POSITION = new Vector3(0.0f, 0.0f, 2.0f);
    private static final float GLOSSY_SHININESS = 12.0f;
    private static final float GLOSSY_MATRIX_SHININESS = 8.0f;
    private static final float GLOSSY_MATRIX_SPECULAR_STRENGTH = 8.0f;
    private static final float GLOSSY_DIFFUSE_BASE = 0.5f;

    private final TransferCache cache = new TransferCache();
    private Vector3 fixedGlossyCameraPosition = DEFAULT_FIXED_GLOSSY_CAMERA_POSITION;

    public TransferBaker setFixedGlossyCameraPosition(Vector3 fixedGlossyCameraPosition) {
        this.fixedGlossyCameraPosition = fixedGlossyCameraPosition == null
                ? DEFAULT_FIXED_GLOSSY_CAMERA_POSITION
                : fixedGlossyCameraPosition;
        return this;
    }

    public ArrayList<TransferData> loadOrBake(String meshResourcePath, Mesh mesh) {
        return loadOrBake(
                meshResourcePath,
                mesh,
                SHCoefficients.DEFAULT_BANDS,
                DEFAULT_SAMPLE_COUNT,
                PRTBakeMode.UNSHADOW,
                PRTReflectionMode.DIFFUSE
        );
    }

    public ArrayList<TransferData> loadOrBake(String meshResourcePath, Mesh mesh, int bands, int sampleCount) {
        return loadOrBake(meshResourcePath, mesh, bands, sampleCount, PRTBakeMode.UNSHADOW, PRTReflectionMode.DIFFUSE);
    }

    public ArrayList<TransferData> loadOrBake(
            String meshResourcePath,
            Mesh mesh,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode
    ) {
        return loadOrBake(meshResourcePath, mesh, bands, sampleCount, bakeMode, PRTReflectionMode.DIFFUSE);
    }

    public ArrayList<TransferData> loadOrBake(
            String meshResourcePath,
            Mesh mesh,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode
    ) {
        mesh.finishBuild();
        PRTBakeMode mode = bakeMode == null ? PRTBakeMode.UNSHADOW : bakeMode;
        PRTReflectionMode reflection = reflectionMode == null ? PRTReflectionMode.DIFFUSE : reflectionMode;

        if (cache.exists(meshResourcePath, bands, sampleCount, mode, reflection)) {
            ArrayList<TransferData> cached = cache.load(meshResourcePath, mesh, bands, sampleCount, mode, reflection);
            applyToMesh(mesh, cached);
            System.out.println("[TransferBaker] Loaded PRT transfer cache: "
                    + cache.cachePath(meshResourcePath, bands, sampleCount, mode, reflection));
            return cached;
        }

        ArrayList<TransferData> baked = bake(mesh, bands, sampleCount, mode, reflection);
        cache.save(meshResourcePath, mesh, baked);
        applyToMesh(mesh, baked);
        System.out.println("[TransferBaker] Baked PRT transfer cache: "
                + cache.cachePath(meshResourcePath, bands, sampleCount, mode, reflection));
        return baked;
    }

    public ArrayList<TransferData> bake(Mesh mesh, int bands, int sampleCount) {
        return bake(mesh, bands, sampleCount, PRTBakeMode.UNSHADOW, PRTReflectionMode.DIFFUSE);
    }

    public ArrayList<TransferData> bake(Mesh mesh, int bands, int sampleCount, PRTBakeMode bakeMode) {
        return bake(mesh, bands, sampleCount, bakeMode, PRTReflectionMode.DIFFUSE);
    }

    public ArrayList<TransferData> bake(
            Mesh mesh,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode
    ) {
        if (sampleCount <= 0) {
            throw new IllegalArgumentException("[TransferBaker] sampleCount must be positive.");
        }

        PRTBakeMode mode = bakeMode == null ? PRTBakeMode.UNSHADOW : bakeMode;
        PRTReflectionMode reflection = reflectionMode == null ? PRTReflectionMode.DIFFUSE : reflectionMode;
        if (reflection == PRTReflectionMode.GLOSSY_MATRIX) {
            return bakeGlossyMatrix(mesh, bands, sampleCount, mode);
        }

        if (mode == PRTBakeMode.INTER_SHADOW) {
            return bakeInterShadow(mesh, bands, sampleCount, reflection, DEFAULT_INTERREFLECTION_BOUNCES);
        }

        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        ArrayList<TransferData> out = new ArrayList<>();
        DirectionSample[] samples = buildSphereSamples(sampleCount, bands);
        TriangleOccluder occluder = mode == PRTBakeMode.SHADOW ? new TriangleOccluder(subMeshes) : null;
        ReflectionWeightFunction reflectionWeightFunction = selectReflectionWeightFunction(reflection);
        TransferWeightFunction weightFunction = selectWeightFunction(mode, occluder, reflectionWeightFunction);
        BakeProgress progress = new BakeProgress(totalVertexCount(subMeshes));

        System.out.println("[TransferBaker] Baking PRT transfer: mode=" + mode
                + ", reflection=" + reflection
                + ", bands=" + bands
                + ", samples=" + sampleCount
                + ", vertices=" + progress.totalVertices);

        for (SubMesh subMesh : subMeshes) {
            out.add(bakeSubMesh(subMesh, bands, sampleCount, mode, reflection, samples, weightFunction, progress));
        }

        progress.finish();
        return out;
    }

    private ArrayList<TransferData> bakeGlossyMatrix(Mesh mesh, int bands, int sampleCount, PRTBakeMode bakeMode) {
        if (bakeMode == PRTBakeMode.INTER_SHADOW) {
            throw new IllegalArgumentException("[TransferBaker] GLOSSY_MATRIX does not support INTER_SHADOW yet.");
        }

        PRTBakeMode mode = bakeMode == null ? PRTBakeMode.UNSHADOW : bakeMode;
        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        ArrayList<TransferData> out = new ArrayList<>();
        DirectionSample[] incomingSamples = buildSphereSamples(sampleCount, bands);
        DirectionSample[] outgoingSamples = buildSphereSamples(sampleCount, bands);
        TriangleOccluder occluder = mode == PRTBakeMode.SHADOW ? new TriangleOccluder(subMeshes) : null;
        BakeProgress progress = new BakeProgress(totalVertexCount(subMeshes));

        System.out.println("[TransferBaker] Baking PRT transfer: mode=" + mode
                + ", reflection=" + PRTReflectionMode.GLOSSY_MATRIX
                + ", bands=" + bands
                + ", samples=" + sampleCount
                + ", vertices=" + progress.totalVertices);

        for (SubMesh subMesh : subMeshes) {
            out.add(bakeGlossyMatrixSubMesh(
                    subMesh,
                    bands,
                    sampleCount,
                    mode,
                    incomingSamples,
                    outgoingSamples,
                    occluder,
                    progress
            ));
        }

        progress.finish();
        return out;
    }

    private TransferData bakeGlossyMatrixSubMesh(
            SubMesh subMesh,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            DirectionSample[] incomingSamples,
            DirectionSample[] outgoingSamples,
            TriangleOccluder occluder,
            BakeProgress progress
    ) {
        int vertexCount = subMesh.getVertexCount();
        int basisCount = bands * bands;
        TransferData data = new TransferData(
                vertexCount,
                bands,
                sampleCount,
                bakeMode,
                PRTReflectionMode.GLOSSY_MATRIX,
                basisCount * basisCount
        );
        float incomingScale = 4.0f * (float) Math.PI / incomingSamples.length;
        float outgoingScale = 4.0f * (float) Math.PI / outgoingSamples.length;

        for (int v = 0; v < vertexCount; v++) {
            Vector3 position = readPosition(subMesh, v);
            Vector3 normal = readNormal(subMesh, v);

            for (DirectionSample incoming : incomingSamples) {
                float ndotl = Math.max(0.0f, Vector3.dot(normal, incoming.direction));
                if (ndotl <= 0.0f) {
                    continue;
                }

                if (bakeMode == PRTBakeMode.SHADOW
                        && (occluder == null || occluder.isOccluded(position, normal, incoming.direction))) {
                    continue;
                }

                for (DirectionSample outgoing : outgoingSamples) {
                    float brdfWeight = computeGlossyMatrixBrdfWeight(normal, incoming.direction, outgoing.direction);
                    if (brdfWeight <= 0.0f) {
                        continue;
                    }

                    float weight = ndotl * brdfWeight * incomingScale * outgoingScale;
                    for (int i = 0; i < basisCount; i++) {
                        for (int j = 0; j < basisCount; j++) {
                            int matrixIndex = i * basisCount + j;
                            float value = incoming.sh[i] * outgoing.sh[j] * weight;
                            data.set(v, matrixIndex, data.get(v, matrixIndex) + value);
                        }
                    }
                }
            }

            progress.vertexDone();
        }

        return data;
    }

    private ArrayList<TransferData> bakeInterShadow(
            Mesh mesh,
            int bands,
            int sampleCount,
            PRTReflectionMode reflectionMode,
            int bounceCount
    ) {
        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        DirectionSample[] samples = buildSphereSamples(sampleCount, bands);
        TriangleOccluder occluder = new TriangleOccluder(subMeshes);
        ReflectionWeightFunction reflectionWeightFunction = selectReflectionWeightFunction(reflectionMode);
        TransferWeightFunction shadowWeightFunction = selectWeightFunction(
                PRTBakeMode.SHADOW,
                occluder,
                reflectionWeightFunction
        );
        int totalVertices = totalVertexCount(subMeshes);
        BakeProgress progress = new BakeProgress(totalVertices * (1 + Math.max(0, bounceCount)));

        System.out.println("[TransferBaker] Baking PRT transfer: mode=" + PRTBakeMode.INTER_SHADOW
                + ", reflection=" + reflectionMode
                + ", bands=" + bands
                + ", samples=" + sampleCount
                + ", bounces=" + bounceCount
                + ", vertices=" + totalVertices);

        ArrayList<TransferData> direct = new ArrayList<>();
        for (SubMesh subMesh : subMeshes) {
            direct.add(bakeSubMesh(
                    subMesh,
                    bands,
                    sampleCount,
                    PRTBakeMode.INTER_SHADOW,
                    reflectionMode,
                    samples,
                    shadowWeightFunction,
                    progress
            ));
        }

        ArrayList<TransferData> current = copyTransferData(direct, PRTBakeMode.INTER_SHADOW, reflectionMode);
        for (int bounce = 0; bounce < bounceCount; bounce++) {
            System.out.println("[TransferBaker] Baking interreflection bounce " + (bounce + 1) + "/" + bounceCount);
            current = bakeInterShadowBounce(
                    subMeshes,
                    current,
                    direct,
                    samples,
                    occluder,
                    reflectionWeightFunction,
                    progress
            );
        }

        progress.finish();
        return current;
    }

    private ArrayList<TransferData> bakeInterShadowBounce(
            ArrayList<SubMesh> subMeshes,
            ArrayList<TransferData> previous,
            ArrayList<TransferData> direct,
            DirectionSample[] samples,
            TriangleOccluder occluder,
            ReflectionWeightFunction reflectionWeightFunction,
            BakeProgress progress
    ) {
        ArrayList<TransferData> next = copyTransferData(direct, PRTBakeMode.INTER_SHADOW, direct.get(0).getReflectionMode());
        float integralScale = 4.0f * (float) Math.PI / samples.length;

        for (int subMeshIndex = 0; subMeshIndex < subMeshes.size(); subMeshIndex++) {
            SubMesh subMesh = subMeshes.get(subMeshIndex);
            TransferData nextData = next.get(subMeshIndex);
            int coefficientCount = nextData.getCoefficientCount();
            int vertexCount = subMesh.getVertexCount();

            for (int v = 0; v < vertexCount; v++) {
                Vector3 position = readPosition(subMesh, v);
                Vector3 normal = readNormal(subMesh, v);

                for (DirectionSample sample : samples) {
                    float outgoingWeight = reflectionWeightFunction.compute(new ReflectionSampleContext(
                            position,
                            normal,
                            sample.direction
                    ));
                    if (outgoingWeight <= 0.0f) {
                        continue;
                    }

                    HitRecord hit = occluder.closestHit(position, normal, sample.direction);
                    if (hit == null) {
                        continue;
                    }

                    float hitCos = Math.max(0.0f, Vector3.dot(hit.normal, sample.direction.mult(-1.0f)));
                    if (hitCos <= 0.0f) {
                        continue;
                    }

                    TransferData hitTransfer = previous.get(hit.subMeshIndex);
                    float weight = DEFAULT_INTERREFLECTION_ALBEDO * outgoingWeight * hitCos * integralScale;
                    for (int k = 0; k < coefficientCount; k++) {
                        float bounced = hitTransfer.get(hit.vertexIndex, k) * weight;
                        nextData.set(v, k, nextData.get(v, k) + bounced);
                    }
                }

                progress.vertexDone();
            }
        }

        return next;
    }

    private TransferData bakeSubMesh(
            SubMesh subMesh,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode,
            DirectionSample[] samples,
            TransferWeightFunction weightFunction,
            BakeProgress progress
    ) {
        int vertexCount = subMesh.getVertexCount();
        TransferData data = new TransferData(vertexCount, bands, sampleCount, bakeMode, reflectionMode);
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

    private TransferWeightFunction selectWeightFunction(
            PRTBakeMode mode,
            TriangleOccluder occluder,
            ReflectionWeightFunction reflectionWeightFunction
    ) {
        return switch (mode) {
            case UNSHADOW -> context -> reflectionWeightFunction.compute(context.toReflectionContext());
            case SHADOW -> context -> computeShadowWeight(context, occluder, reflectionWeightFunction);
            case INTER_SHADOW -> throw new IllegalArgumentException("[TransferBaker] INTER_SHADOW uses bounce baking.");
        };
    }

    private ReflectionWeightFunction selectReflectionWeightFunction(PRTReflectionMode reflectionMode) {
        return switch (reflectionMode) {
            case DIFFUSE -> this::computeDiffuseWeight;
            case GLOSSY -> this::computeGlossyWeight;
            case GLOSSY_MATRIX -> this::computeGlossyWeight;
        };
    }

    private float computeDiffuseWeight(ReflectionSampleContext context) {
        return Math.max(0.0f, Vector3.dot(context.normal, context.direction));
    }

    private float computeGlossyWeight(ReflectionSampleContext context) {
        float ndotl = computeDiffuseWeight(context);
        if (ndotl <= 0.0f) {
            return 0.0f;
        }

        Vector3 incoming = context.direction.mult(-1.0f);
        Vector3 reflected = Vector3.reflect(incoming, context.normal).unit_vector();
        Vector3 viewDirection = FIXED_GLOSSY_VIEW_DIRECTION.unit_vector();
        float specular = (float) Math.pow(
                Math.max(0.0f, Vector3.dot(reflected, viewDirection)),
                GLOSSY_SHININESS
        );

        return ndotl * (GLOSSY_DIFFUSE_BASE + specular);
    }

    private float computeGlossyBrdfWeight(Vector3 normal, Vector3 incomingDirection, Vector3 outgoingDirection) {
        Vector3 incoming = incomingDirection.mult(-1.0f);
        Vector3 reflected = Vector3.reflect(incoming, normal).unit_vector();
        float specular = (float) Math.pow(
                Math.max(0.0f, Vector3.dot(reflected, outgoingDirection.unit_vector())),
                GLOSSY_SHININESS
        );
        return GLOSSY_DIFFUSE_BASE + specular;
    }

    private float computeGlossyMatrixBrdfWeight(Vector3 normal, Vector3 incomingDirection, Vector3 outgoingDirection) {
        Vector3 incoming = incomingDirection.mult(-1.0f);
        Vector3 reflected = Vector3.reflect(incoming, normal).unit_vector();
        float specular = (float) Math.pow(
                Math.max(0.0f, Vector3.dot(reflected, outgoingDirection.unit_vector())),
                GLOSSY_MATRIX_SHININESS
        );
        return specular * GLOSSY_MATRIX_SPECULAR_STRENGTH;
    }

    private float computeShadowWeight(
            TransferSampleContext context,
            TriangleOccluder occluder,
            ReflectionWeightFunction reflectionWeightFunction
    ) {
        float reflectionWeight = reflectionWeightFunction.compute(context.toReflectionContext());
        if (reflectionWeight <= 0.0f) {
            return 0.0f;
        }

        if (occluder == null || occluder.isOccluded(context.position, context.normal, context.direction)) {
            return 0.0f;
        }

        return reflectionWeight;
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

    private ArrayList<TransferData> copyTransferData(
            ArrayList<TransferData> source,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode
    ) {
        ArrayList<TransferData> out = new ArrayList<>();
        for (TransferData data : source) {
            TransferData copied = new TransferData(
                    data.getVertexCount(),
                    data.getBands(),
                    data.getSampleCount(),
                    bakeMode,
                    reflectionMode
            );
            System.arraycopy(data.raw(), 0, copied.raw(), 0, data.raw().length);
            out.add(copied);
        }
        return out;
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
            subMesh.prtReflectionMode = data.getReflectionMode();
            subMesh.prtCoefficients = data.getCoefficientCount() == data.getBands() * data.getBands()
                    ? data.raw()
                    : collapseMatrixToFixedViewVector(data, subMesh);
        }
    }

    private float[] collapseMatrixToFixedViewVector(TransferData data, SubMesh subMesh) {
        int basisCount = data.getBands() * data.getBands();
        int matrixCount = basisCount * basisCount;
        if (data.getCoefficientCount() != matrixCount) {
            return data.raw();
        }

        float[] out = new float[data.getVertexCount() * basisCount];

        for (int v = 0; v < data.getVertexCount(); v++) {
            Vector3 position = readPosition(subMesh, v);
            Vector3 viewDirection = fixedGlossyCameraPosition.sub(position).unit_vector();
            float[] viewBasis = evaluateSHBasis(data.getBands(), viewDirection);

            for (int i = 0; i < basisCount; i++) {
                float value = 0.0f;
                for (int j = 0; j < basisCount; j++) {
                    value += data.get(v, i * basisCount + j) * viewBasis[j];
                }
                out[v * basisCount + i] = value;
            }
        }

        return out;
    }

    private float[] evaluateSHBasis(int bands, Vector3 direction) {
        float[] out = new float[bands * bands];
        int index = 0;
        for (int l = 0; l < bands; l++) {
            for (int m = -l; m <= l; m++) {
                out[index++] = SphereHarmonic.EvalSH(l, m, direction);
            }
        }
        return out;
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

    @FunctionalInterface
    private interface ReflectionWeightFunction {
        float compute(ReflectionSampleContext context);
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

        ReflectionSampleContext toReflectionContext() {
            return new ReflectionSampleContext(position, normal, direction);
        }
    }

    private static class ReflectionSampleContext {
        final Vector3 position;
        final Vector3 normal;
        final Vector3 direction;

        ReflectionSampleContext(Vector3 position, Vector3 normal, Vector3 direction) {
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
            for (int i = 0; i < subMeshes.size(); i++) {
                collectTriangles(subMeshes.get(i), i);
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

        HitRecord closestHit(Vector3 position, Vector3 normal, Vector3 direction) {
            if (root == null) {
                return null;
            }

            Vector3 origin = position.add(normal.mult(ORIGIN_BIAS));
            return root.closestHit(origin, direction, RAY_EPSILON, Float.POSITIVE_INFINITY);
        }

        private void collectTriangles(SubMesh subMesh, int subMeshIndex) {
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
                        subMeshIndex,
                        i0,
                        i1,
                        i2,
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

        private static HitRecord intersectTriangleHit(
                Vector3 origin,
                Vector3 direction,
                Triangle triangle,
                float tMin,
                float tMax
        ) {
            Vector3 edge1 = triangle.v1.sub(triangle.v0);
            Vector3 edge2 = triangle.v2.sub(triangle.v0);
            Vector3 pvec = Vector3.cross(direction, edge2);
            float det = Vector3.dot(edge1, pvec);

            if (Math.abs(det) < RAY_EPSILON) {
                return null;
            }

            float invDet = 1.0f / det;
            Vector3 tvec = origin.sub(triangle.v0);
            float u = Vector3.dot(tvec, pvec) * invDet;
            if (u < 0.0f || u > 1.0f) {
                return null;
            }

            Vector3 qvec = Vector3.cross(tvec, edge1);
            float v = Vector3.dot(direction, qvec) * invDet;
            if (v < 0.0f || u + v > 1.0f) {
                return null;
            }

            float t = Vector3.dot(edge2, qvec) * invDet;
            if (t <= tMin || t >= tMax) {
                return null;
            }

            return new HitRecord(triangle, t, u, v);
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

            HitRecord closestHit(Vector3 origin, Vector3 direction, float tMin, float tMax) {
                if (!bounds.intersects(origin, direction, tMin, tMax)) {
                    return null;
                }

                HitRecord closest = null;
                if (leafTriangles != null) {
                    for (Triangle triangle : leafTriangles) {
                        HitRecord hit = intersectTriangleHit(origin, direction, triangle, tMin, tMax);
                        if (hit != null) {
                            closest = hit;
                            tMax = hit.t;
                        }
                    }
                    return closest;
                }

                HitRecord hitA = childA.closestHit(origin, direction, tMin, tMax);
                if (hitA != null) {
                    closest = hitA;
                    tMax = hitA.t;
                }

                HitRecord hitB = childB.closestHit(origin, direction, tMin, tMax);
                if (hitB != null && (closest == null || hitB.t < closest.t)) {
                    closest = hitB;
                }

                return closest;
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
        final int subMeshIndex;
        final int i0;
        final int i1;
        final int i2;
        final Vector3 v0;
        final Vector3 v1;
        final Vector3 v2;
        final Vector3 center;
        final Vector3 normal;

        Triangle(int subMeshIndex, int i0, int i1, int i2, Vector3 v0, Vector3 v1, Vector3 v2) {
            this.subMeshIndex = subMeshIndex;
            this.i0 = i0;
            this.i1 = i1;
            this.i2 = i2;
            this.v0 = v0;
            this.v1 = v1;
            this.v2 = v2;
            this.center = new Vector3(
                    (v0.x + v1.x + v2.x) / 3.0f,
                    (v0.y + v1.y + v2.y) / 3.0f,
                    (v0.z + v1.z + v2.z) / 3.0f
            );
            this.normal = Vector3.cross(v1.sub(v0), v2.sub(v0)).unit_vector();
        }
    }

    private static class HitRecord {
        final int subMeshIndex;
        final int vertexIndex;
        final Vector3 normal;
        final float t;

        HitRecord(Triangle triangle, float t, float u, float v) {
            float w = 1.0f - u - v;
            this.subMeshIndex = triangle.subMeshIndex;
            this.vertexIndex = closestVertexIndex(triangle, w, u, v);
            this.normal = triangle.normal;
            this.t = t;
        }

        private static int closestVertexIndex(Triangle triangle, float w, float u, float v) {
            if (w >= u && w >= v) {
                return triangle.i0;
            }
            return u >= v ? triangle.i1 : triangle.i2;
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
