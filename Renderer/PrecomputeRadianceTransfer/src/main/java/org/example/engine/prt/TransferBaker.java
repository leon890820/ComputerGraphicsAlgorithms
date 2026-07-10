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
        return loadOrBake(meshResourcePath, mesh, SHCoefficients.DEFAULT_BANDS, DEFAULT_SAMPLE_COUNT);
    }

    public ArrayList<TransferData> loadOrBake(String meshResourcePath, Mesh mesh, int bands, int sampleCount) {
        mesh.finishBuild();

        if (cache.exists(meshResourcePath, bands, sampleCount)) {
            ArrayList<TransferData> cached = cache.load(meshResourcePath,mesh, bands, sampleCount);
            applyToMesh(mesh, cached);
            System.out.println("[TransferBaker] Loaded PRT transfer cache: "
                    + cache.cachePath(meshResourcePath, bands, sampleCount));
            return cached;
        }

        ArrayList<TransferData> baked = bake(mesh, bands, sampleCount);
        cache.save(meshResourcePath, mesh, baked);
        applyToMesh(mesh, baked);
        System.out.println("[TransferBaker] Baked PRT transfer cache: "
                + cache.cachePath(meshResourcePath, bands, sampleCount));
        return baked;
    }

    public ArrayList<TransferData> bake(Mesh mesh, int bands, int sampleCount) {
        if (sampleCount <= 0) {
            throw new IllegalArgumentException("[TransferBaker] sampleCount must be positive.");
        }

        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        ArrayList<TransferData> out = new ArrayList<>();
        DirectionSample[] samples = buildSphereSamples(sampleCount, bands);
        BakeProgress progress = new BakeProgress(totalVertexCount(subMeshes));

        System.out.println("[TransferBaker] Baking PRT transfer: bands=" + bands
                + ", samples=" + sampleCount
                + ", vertices=" + progress.totalVertices);

        for (SubMesh subMesh : subMeshes) {
            out.add(bakeSubMesh(subMesh, bands, sampleCount, samples, progress));
        }

        progress.finish();
        return out;
    }

    private TransferData bakeSubMesh(
            SubMesh subMesh,
            int bands,
            int sampleCount,
            DirectionSample[] samples,
            BakeProgress progress
    ) {
        int vertexCount = subMesh.getVertexCount();
        TransferData data = new TransferData(vertexCount, bands, sampleCount);
        int coefficientCount = data.getCoefficientCount();
        float integralScale = 4.0f * (float) Math.PI / sampleCount;

        for (int v = 0; v < vertexCount; v++) {
            Vector3 normal = readNormal(subMesh, v);

            for (DirectionSample sample : samples) {
                float ndotl = Math.max(0.0f, Vector3.dot(normal, sample.direction));
                if (ndotl <= 0.0f) {
                    continue;
                }

                float weight = ndotl * integralScale;
                for (int k = 0; k < coefficientCount; k++) {
                    data.set(v, k, data.get(v, k) + sample.sh[k] * weight);
                }
            }

            progress.vertexDone();
        }

        return data;
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
