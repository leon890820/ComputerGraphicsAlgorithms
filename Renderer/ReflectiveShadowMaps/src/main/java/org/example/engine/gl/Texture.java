package org.example.engine.gl;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Texture {

    IntBuffer tex;

    int width = 0;
    int height = 0;
    boolean uploaded = false;

    boolean flipYOnUpload = true;

    public Texture() {
        tex = MemoryUtil.memAllocInt(1);
        glGenTextures(tex);
    }

    public Texture(int w, int h) {
        this();

        width = w;
        height = h;

        glBindTexture(GL_TEXTURE_2D, tex.get(0));

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA32F,
                w,
                h,
                0,
                GL_RGBA,
                GL_FLOAT,
                0
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);

        uploaded = true;
    }

    public Texture(String path) {
        this();
        setTexture(path);
    }

    public Texture(String path, boolean flipY) {
        this();
        this.flipYOnUpload = flipY;
        setTexture(path);
    }

    public Texture setFlipYOnUpload(boolean flipY) {
        this.flipYOnUpload = flipY;
        return this;
    }

    public Texture setTexture(String path) {
        STBImage.stbi_set_flip_vertically_on_load(flipYOnUpload);

        try {
            InputStream input = Texture.class.getResourceAsStream(path);

            if (input == null && !path.startsWith("/")) {
                input = Texture.class.getResourceAsStream("/" + path);
            }

            byte[] bytes;

            // ===== 先走 resource =====
            if (input != null) {
                try (InputStream in = input) {
                    bytes = in.readAllBytes();
                }
            }
            // ===== fallback：走檔案系統 =====
            else {
                java.io.File file = new java.io.File(path);

                System.out.println("[Texture] Resource not found, try file:");
                System.out.println("Absolute = " + file.getAbsolutePath());
                System.out.println("Canonical = " + file.getCanonicalPath());
                System.out.println("Exists = " + file.exists());

                if (!file.exists()) {
                    System.out.println("[Texture] Failed to load image: " + path);
                    return this;
                }

                bytes = java.nio.file.Files.readAllBytes(file.toPath());
            }

            ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length);
            imageBuffer.put(bytes);
            imageBuffer.flip();

            IntBuffer w = MemoryUtil.memAllocInt(1);
            IntBuffer h = MemoryUtil.memAllocInt(1);
            IntBuffer channels = MemoryUtil.memAllocInt(1);

            ByteBuffer image = STBImage.stbi_load_from_memory(
                    imageBuffer,
                    w,
                    h,
                    channels,
                    4
            );

            MemoryUtil.memFree(imageBuffer);

            if (image == null) {
                System.out.println("[Texture] STB failed: " + STBImage.stbi_failure_reason());
                MemoryUtil.memFree(w);
                MemoryUtil.memFree(h);
                MemoryUtil.memFree(channels);
                return this;
            }

            width = w.get(0);
            height = h.get(0);

            uploadImageToGPU(image, width, height);

            STBImage.stbi_image_free(image);

            MemoryUtil.memFree(w);
            MemoryUtil.memFree(h);
            MemoryUtil.memFree(channels);

            return this;

        } catch (Exception e) {
            throw new RuntimeException("[Texture] Failed to load image: " + path, e);
        }
    }

    /**
     * 純 Java 版沒有 PImage，所以這個接口先不保留 PImage 參數。
     * 如果之後你真的要從 byte[] 或 BufferedImage 建 texture，可以再補 overload。
     */
    private void uploadImageToGPU(ByteBuffer image, int w, int h) {
        if (image == null) {
            System.out.println("[Texture] upload failed: image is null");
            return;
        }

        glBindTexture(GL_TEXTURE_2D, tex.get(0));

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                w,
                h,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                image
        );

        // 不產生 mipmap
        // glGenerateMipmap(GL_TEXTURE_2D);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glBindTexture(GL_TEXTURE_2D, 0);

        uploaded = true;
    }

    public Texture bind(int unit) {
        if (tex == null) return this;

        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, tex.get(0));
        return this;
    }

    public Texture unbind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, 0);
        return this;
    }

    public Texture setWrapMode(int mode) {
        if (tex == null) return this;

        glBindTexture(GL_TEXTURE_2D, tex.get(0));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, mode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, mode);
        glBindTexture(GL_TEXTURE_2D, 0);

        return this;
    }

    public Texture setSamplingMode(int mode) {
        if (tex == null) return this;

        glBindTexture(GL_TEXTURE_2D, tex.get(0));
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mode);
        glBindTexture(GL_TEXTURE_2D, 0);

        return this;
    }

    public int getID() {
        if (tex == null) return 0;
        return tex.get(0);
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void dispose() {
        if (tex != null) {
            glDeleteTextures(tex);
            MemoryUtil.memFree(tex);
            tex = null;
        }

        uploaded = false;
        width = 0;
        height = 0;
    }
}