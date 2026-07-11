package org.example.engine.prt;

public class TransferData {

    private final int bands;
    private final int sampleCount;
    private final PRTBakeMode bakeMode;
    private final PRTReflectionMode reflectionMode;
    private final int coefficientCount;
    private final float[] coefficients;

    public TransferData(int vertexCount, int bands, int sampleCount) {
        this(vertexCount, bands, sampleCount, PRTBakeMode.UNSHADOW);
    }

    public TransferData(int vertexCount, int bands, int sampleCount, PRTBakeMode bakeMode) {
        this(vertexCount, bands, sampleCount, bakeMode, PRTReflectionMode.DIFFUSE);
    }

    public TransferData(
            int vertexCount,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode
    ) {
        this(vertexCount, bands, sampleCount, bakeMode, reflectionMode, bands * bands);
    }

    public TransferData(
            int vertexCount,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode,
            int coefficientCount
    ) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("[TransferData] vertexCount must not be negative.");
        }
        if (bands <= 0) {
            throw new IllegalArgumentException("[TransferData] bands must be positive.");
        }
        if (coefficientCount <= 0) {
            throw new IllegalArgumentException("[TransferData] coefficientCount must be positive.");
        }

        this.bands = bands;
        this.sampleCount = sampleCount;
        this.bakeMode = bakeMode == null ? PRTBakeMode.UNSHADOW : bakeMode;
        this.reflectionMode = reflectionMode == null ? PRTReflectionMode.DIFFUSE : reflectionMode;
        this.coefficientCount = coefficientCount;
        this.coefficients = new float[vertexCount * coefficientCount];
    }

    public int getBands() {
        return bands;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public PRTBakeMode getBakeMode() {
        return bakeMode;
    }

    public PRTReflectionMode getReflectionMode() {
        return reflectionMode;
    }

    public int getCoefficientCount() {
        return coefficientCount;
    }

    public int getVertexCount() {
        return coefficients.length / getCoefficientCount();
    }

    public float[] raw() {
        return coefficients;
    }

    public void set(int vertexIndex, int coefficientIndex, float value) {
        coefficients[offset(vertexIndex, coefficientIndex)] = value;
    }

    public float get(int vertexIndex, int coefficientIndex) {
        return coefficients[offset(vertexIndex, coefficientIndex)];
    }

    private int offset(int vertexIndex, int coefficientIndex) {
        return vertexIndex * getCoefficientCount() + coefficientIndex;
    }
}
