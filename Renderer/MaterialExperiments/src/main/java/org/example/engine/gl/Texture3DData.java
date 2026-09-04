package org.example.engine.gl;

import org.lwjgl.system.MemoryUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;

public class Texture3DData {
    private static final int MAGIC = 0x4D335444; // M3TD
    private static final int VERSION = 1;
    private static final int CHANNELS = 4;

    private final int width;
    private final int height;
    private final int depth;
    private final float[] pixels;

    public Texture3DData(int width, int height, int depth) {
        if (width <= 0 || height <= 0 || depth <= 0) {
            throw new IllegalArgumentException("Texture3D dimensions must be positive.");
        }

        this.width = width;
        this.height = height;
        this.depth = depth;
        this.pixels = new float[width * height * depth * CHANNELS];
    }

    private Texture3DData(int width, int height, int depth, float[] pixels) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.pixels = pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public float[] getPixels() {
        return pixels;
    }

    public Texture3DData setVoxel(int x, int y, int z, float r, float g, float b, float a) {
        int index = indexOf(x, y, z);
        pixels[index] = r;
        pixels[index + 1] = g;
        pixels[index + 2] = b;
        pixels[index + 3] = a;
        return this;
    }

    public Texture3DData setVoxel(int x, int y, int z, float value) {
        return setVoxel(x, y, z, value, value, value, 1.0f);
    }

    public float[] getVoxel(int x, int y, int z) {
        int index = indexOf(x, y, z);
        return new float[] {
                pixels[index],
                pixels[index + 1],
                pixels[index + 2],
                pixels[index + 3]
        };
    }

    public Texture3D upload() {
        return upload(GL_LINEAR);
    }

    public Texture3D uploadNearest() {
        return upload(GL_NEAREST);
    }

    public Texture3D upload(int filter) {
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(pixels.length * Float.BYTES);
        try {
            FloatBuffer floatBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
            floatBuffer.put(pixels);
            floatBuffer.flip();
            return new Texture3D(width, height, depth, GL_RGBA32F, GL_RGBA, GL_FLOAT, filter, byteBuffer);
        } finally {
            MemoryUtil.memFree(byteBuffer);
        }
    }

    public void save(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(width);
            out.writeInt(height);
            out.writeInt(depth);
            out.writeInt(CHANNELS);
            for (float pixel : pixels) {
                out.writeFloat(pixel);
            }
        }
    }

    public static Texture3DData load(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            int magic = in.readInt();
            int version = in.readInt();
            if (magic != MAGIC || version != VERSION) {
                throw new IOException("Unsupported Texture3DData file: " + path);
            }

            int width = in.readInt();
            int height = in.readInt();
            int depth = in.readInt();
            int channels = in.readInt();
            if (width <= 0 || height <= 0 || depth <= 0 || channels != CHANNELS) {
                throw new IOException("Invalid Texture3DData header: " + path);
            }

            float[] pixels = new float[width * height * depth * CHANNELS];
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = in.readFloat();
            }
            return new Texture3DData(width, height, depth, pixels);
        }
    }

    private int indexOf(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            throw new IndexOutOfBoundsException("Voxel index out of bounds: " + x + ", " + y + ", " + z);
        }
        return ((z * height + y) * width + x) * CHANNELS;
    }
}


