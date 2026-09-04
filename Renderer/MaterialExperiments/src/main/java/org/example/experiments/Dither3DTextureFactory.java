package org.example.experiments;

import org.example.engine.gl.Texture;
import org.example.engine.gl.Texture3DData;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public final class Dither3DTextureFactory {
    private static final int BAYER_8X8_RECURSION = 3;
    private static final int BRIGHTNESS_BUCKET_COUNT = 256;

    private Dither3DTextureFactory() {
    }

    public static Texture3DData createBayer8x8() {
        return createBayerTexture(BAYER_8X8_RECURSION);
    }

    public static Texture3DData loadOrCreateBayer8x8(Path path) throws IOException {
        Path resolvedPath = path.toAbsolutePath().normalize();
        long start = System.nanoTime();

        if (Files.exists(resolvedPath)) {
            Texture3DData data = Texture3DData.load(resolvedPath);
            logElapsed("loaded", resolvedPath, start);
            return data;
        }

        System.out.println("[Dither3DTextureFactory] generating: " + resolvedPath);
        Texture3DData data = createBayer8x8();
        data.save(resolvedPath);
        logElapsed("generated", resolvedPath, start);
        return data;
    }

    public static Texture createRampTexture(Texture3DData ditherData) {
        float[] lookupRamp = createLookupRamp(ditherData);
        ByteBuffer pixels = MemoryUtil.memAlloc(lookupRamp.length * 4);
        try {
            for (float value : lookupRamp) {
                int channel = Math.round(clamp01(value) * 255.0f);
                pixels.put((byte) channel);
                pixels.put((byte) channel);
                pixels.put((byte) channel);
                pixels.put((byte) 255);
            }
            pixels.flip();

            return new Texture()
                    .setRawRGBA(pixels, lookupRamp.length, 1)
                    .setWrapMode(GL_CLAMP_TO_EDGE)
                    .setSamplingMode(GL_LINEAR);
        } finally {
            MemoryUtil.memFree(pixels);
        }
    }

    public static float[] createLookupRamp(Texture3DData ditherData) {
        int[] brightnessBuckets = createBrightnessBuckets(ditherData);
        float[] brightnessRamp = new float[brightnessBuckets.length + 1];
        int sum = 0;
        int pixelCount = ditherData.getWidth() * ditherData.getHeight() * ditherData.getDepth();

        for (int i = 0; i < brightnessBuckets.length; i++) {
            sum += brightnessBuckets[brightnessBuckets.length - 1 - i];
            brightnessRamp[i + 1] = sum / (float) pixelCount;
        }

        int size = ditherData.getWidth();
        float[] lookupRamp = new float[size];
        float lowerIndexBrightness = 0.0f;
        int higherIndex = 1;
        float higherIndexBrightness = brightnessRamp[higherIndex];

        for (int i = 0; i < size; i++) {
            float desiredBrightness = i / (float) (size - 1);
            while (higherIndex < brightnessRamp.length - 1 && higherIndexBrightness < desiredBrightness) {
                higherIndex++;
                higherIndexBrightness = brightnessRamp[higherIndex];
            }

            float l = inverseLerp(lowerIndexBrightness, higherIndexBrightness, desiredBrightness);
            lookupRamp[i] = (higherIndex - 1 + l) / (brightnessRamp.length - 1);
        }

        return lookupRamp;
    }

    public static Texture3DData createBayerTexture(int recursion) {
        ArrayList<Point2> bayerPoints = createBayerPoints(recursion);

        int dotsPerSide = Math.round((float) Math.pow(2.0, recursion));
        int layers = dotsPerSide * dotsPerSide;
        int size = 16 * dotsPerSide;

        Texture3DData texture = new Texture3DData(size, size, layers);
        float invRes = 1.0f / size;

        for (int z = 0; z < layers; z++) {
            int dotCount = z + 1;
            float dotArea = 0.5f / dotCount;
            float dotRadius = (float) Math.sqrt(dotArea / Math.PI);

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    float px = (x + 0.5f) * invRes;
                    float py = (y + 0.5f) * invRes;
                    float dist = Float.POSITIVE_INFINITY;

                    for (int i = 0; i < dotCount; i++) {
                        Point2 point = bayerPoints.get(i);
                        float vx = repeat(px - point.x + 0.5f, 1.0f) - 0.5f;
                        float vy = repeat(py - point.y + 0.5f, 1.0f) - 0.5f;
                        float curDist = (float) Math.sqrt(vx * vx + vy * vy);
                        dist = Math.min(dist, curDist);
                    }

                    dist = dist / (dotRadius * 2.4f);
                    float value = clamp01(1.0f - dist);
                    texture.setVoxel(x, y, z, value);
                }
            }
        }

        return texture;
    }

    private static void logElapsed(String action, Path path, long start) {
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
        System.out.println("[Dither3DTextureFactory] " + action + " in " + elapsedMs + " ms: " + path);
    }

    private static int[] createBrightnessBuckets(Texture3DData ditherData) {
        int[] buckets = new int[BRIGHTNESS_BUCKET_COUNT];
        float[] pixels = ditherData.getPixels();

        for (int i = 0; i < pixels.length; i += 4) {
            int bucket = clamp((int) (pixels[i] * BRIGHTNESS_BUCKET_COUNT), 0, BRIGHTNESS_BUCKET_COUNT - 1);
            buckets[bucket]++;
        }

        return buckets;
    }

    private static ArrayList<Point2> createBayerPoints(int recursion) {
        ArrayList<Point2> points = new ArrayList<>();
        points.add(new Point2(0.0f, 0.0f));
        points.add(new Point2(0.5f, 0.5f));
        points.add(new Point2(0.5f, 0.0f));
        points.add(new Point2(0.0f, 0.5f));

        for (int r = 0; r < recursion - 1; r++) {
            int count = points.size();
            float offset = (float) Math.pow(0.5f, r + 1);

            for (int i = 1; i < 4; i++) {
                Point2 base = points.get(i);
                for (int j = 0; j < count; j++) {
                    Point2 current = points.get(j);
                    points.add(new Point2(
                            current.x + base.x * offset,
                            current.y + base.y * offset
                    ));
                }
            }
        }

        return points;
    }

    private static float repeat(float value, float length) {
        return value - (float) Math.floor(value / length) * length;
    }

    private static float inverseLerp(float a, float b, float value) {
        float denominator = b - a;
        if (Math.abs(denominator) < 0.000001f) {
            return 0.0f;
        }
        return clamp01((value - a) / denominator);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static final class Point2 {
        final float x;
        final float y;

        Point2(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
