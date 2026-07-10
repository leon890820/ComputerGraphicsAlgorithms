package org.example.engine.prt;

public class TransferData {

    private final int bands;
    private final int sampleCount;
    private final float[] coefficients;

    public TransferData(int vertexCount, int bands, int sampleCount) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("[TransferData] vertexCount must not be negative.");
        }
        if (bands <= 0) {
            throw new IllegalArgumentException("[TransferData] bands must be positive.");
        }

        this.bands = bands;
        this.sampleCount = sampleCount;
        this.coefficients = new float[vertexCount * bands * bands];
    }

    public int getBands() {
        return bands;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public int getCoefficientCount() {
        return bands * bands;
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
