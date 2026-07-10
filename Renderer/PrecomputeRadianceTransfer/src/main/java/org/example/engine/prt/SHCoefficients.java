package org.example.engine.prt;

import java.util.Locale;

public class SHCoefficients {

    public static final int DEFAULT_BANDS = 3;

    private final int bands;
    private final float[] rgb;

    public SHCoefficients() {
        this(DEFAULT_BANDS);
    }

    public SHCoefficients(int bands) {
        if (bands <= 0) {
            throw new IllegalArgumentException("[SHCoefficients] bands must be positive.");
        }

        this.bands = bands;
        this.rgb = new float[bands * bands * 3];
    }

    public int getBands() {
        return bands;
    }

    public int getCoefficientCount() {
        return bands * bands;
    }

    public float[] raw() {
        return rgb;
    }

    public void add(int coefficientIndex, float r, float g, float b) {
        int offset = coefficientIndex * 3;
        rgb[offset] += r;
        rgb[offset + 1] += g;
        rgb[offset + 2] += b;
    }

    public float r(int coefficientIndex) {
        return rgb[coefficientIndex * 3];
    }

    public float g(int coefficientIndex) {
        return rgb[coefficientIndex * 3 + 1];
    }

    public float b(int coefficientIndex) {
        return rgb[coefficientIndex * 3 + 2];
    }

    public String toCacheString(String sourcePath) {
        StringBuilder out = new StringBuilder();
        out.append("# Skybox spherical harmonics coefficients\n");
        out.append("source=").append(sourcePath).append('\n');
        out.append("bands=").append(bands).append('\n');
        out.append("coefficientCount=").append(getCoefficientCount()).append('\n');
        out.append("layout=index r g b\n");

        for (int i = 0; i < getCoefficientCount(); i++) {
            out.append(i)
                    .append(' ')
                    .append(format(r(i)))
                    .append(' ')
                    .append(format(g(i)))
                    .append(' ')
                    .append(format(b(i)))
                    .append('\n');
        }

        return out.toString();
    }

    private String format(float value) {
        return String.format(Locale.US, "%.9g", value);
    }
}
