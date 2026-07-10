package org.example.engine.prt;

import org.example.engine.math.SphereHarmonic;
import org.example.engine.math.Vector3;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class SkyboxSHProjector {

    private static final String[] FACE_NAMES = {
            "posx.jpg",
            "negx.jpg",
            "posy.jpg",
            "negy.jpg",
            "posz.jpg",
            "negz.jpg"
    };

    private final SkyboxSHCache cache = new SkyboxSHCache();

    public SHCoefficients loadOrProject(String skyboxResourcePath) {
        return loadOrProject(skyboxResourcePath, SHCoefficients.DEFAULT_BANDS);
    }

    public SHCoefficients loadOrProject(String skyboxResourcePath, int bands) {
        if (cache.exists(skyboxResourcePath, bands)) {
            return cache.load(skyboxResourcePath, bands);
        }

        SHCoefficients coefficients = project(skyboxResourcePath, bands);
        cache.save(skyboxResourcePath, coefficients);
        return coefficients;
    }

    public SHCoefficients project(String skyboxResourcePath) {
        return project(skyboxResourcePath, SHCoefficients.DEFAULT_BANDS);
    }

    public SHCoefficients project(String skyboxResourcePath, int bands) {
        SHCoefficients out = new SHCoefficients(bands);
        String basePath = trimTrailingSlash(skyboxResourcePath);

        for (int face = 0; face < FACE_NAMES.length; face++) {
            projectFace(out, face, basePath + "/" + FACE_NAMES[face]);
        }

        return out;
    }

    private void projectFace(SHCoefficients out, int faceIndex, String imagePath) {
        ImageData image = loadImage(imagePath);

        try {
            float du = 2.0f / image.width;
            float dv = 2.0f / image.height;

            for (int y = 0; y < image.height; y++) {
                float v = 1.0f - (2.0f * (y + 0.5f) / image.height);

                for (int x = 0; x < image.width; x++) {
                    float u = 2.0f * (x + 0.5f) / image.width - 1.0f;
                    float solidAngle = solidAngle(u, v, du, dv);
                    Vector3 dir = faceDirection(faceIndex, u, v);

                    int pixel = (y * image.width + x) * 4;
                    float r = unsignedByte(image.pixels.get(pixel));
                    float g = unsignedByte(image.pixels.get(pixel + 1));
                    float b = unsignedByte(image.pixels.get(pixel + 2));

                    accumulate(out, dir, solidAngle, r, g, b);
                }
            }
        } finally {
            image.free();
        }
    }

    private void accumulate(SHCoefficients out, Vector3 dir, float weight, float r, float g, float b) {
        int index = 0;
        for (int l = 0; l < out.getBands(); l++) {
            for (int m = -l; m <= l; m++) {
                float sh = SphereHarmonic.EvalSH(l, m, dir);
                out.add(index, r * sh * weight, g * sh * weight, b * sh * weight);
                index++;
            }
        }
    }

    private float solidAngle(float u, float v, float du, float dv) {
        float d = 1.0f + u * u + v * v;
        return du * dv / (float) Math.pow(d, 1.5);
    }

    private Vector3 faceDirection(int faceIndex, float u, float v) {
        switch (faceIndex) {
            case 0:
                return new Vector3(1.0f, v, -u).unit_vector();
            case 1:
                return new Vector3(-1.0f, v, u).unit_vector();
            case 2:
                return new Vector3(u, 1.0f, -v).unit_vector();
            case 3:
                return new Vector3(u, -1.0f, v).unit_vector();
            case 4:
                return new Vector3(u, v, 1.0f).unit_vector();
            case 5:
                return new Vector3(-u, v, -1.0f).unit_vector();
            default:
                throw new IllegalArgumentException("[SkyboxSHProjector] invalid face index: " + faceIndex);
        }
    }

    private float unsignedByte(byte value) {
        return (value & 0xFF) / 255.0f;
    }

    private ImageData loadImage(String path) {
        ByteBuffer encoded = null;
        IntBuffer w = null;
        IntBuffer h = null;
        IntBuffer channels = null;

        try {
            byte[] bytes = readImageBytes(path);
            encoded = MemoryUtil.memAlloc(bytes.length);
            encoded.put(bytes);
            encoded.flip();

            w = MemoryUtil.memAllocInt(1);
            h = MemoryUtil.memAllocInt(1);
            channels = MemoryUtil.memAllocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(false);
            ByteBuffer pixels = STBImage.stbi_load_from_memory(encoded, w, h, channels, 4);
            if (pixels == null) {
                throw new RuntimeException("[SkyboxSHProjector] STB failed: " + STBImage.stbi_failure_reason());
            }

            return new ImageData(pixels, w.get(0), h.get(0));
        } catch (Exception e) {
            throw new RuntimeException("[SkyboxSHProjector] Failed to load skybox face: " + path, e);
        } finally {
            if (encoded != null) MemoryUtil.memFree(encoded);
            if (w != null) MemoryUtil.memFree(w);
            if (h != null) MemoryUtil.memFree(h);
            if (channels != null) MemoryUtil.memFree(channels);
        }
    }

    private byte[] readImageBytes(String path) throws Exception {
        InputStream input = SkyboxSHProjector.class.getResourceAsStream(path);

        if (input == null && !path.startsWith("/")) {
            input = SkyboxSHProjector.class.getResourceAsStream("/" + path);
        }

        if (input != null) {
            try (InputStream in = input) {
                return in.readAllBytes();
            }
        }

        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            throw new RuntimeException("file does not exist");
        }

        return java.nio.file.Files.readAllBytes(file.toPath());
    }

    private String trimTrailingSlash(String path) {
        while (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static class ImageData {
        final ByteBuffer pixels;
        final int width;
        final int height;

        ImageData(ByteBuffer pixels, int width, int height) {
            this.pixels = pixels;
            this.width = width;
            this.height = height;
        }

        void free() {
            STBImage.stbi_image_free(pixels);
        }
    }
}
