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
    private boolean useMipmap = false;

    public Texture() {
        tex = MemoryUtil.memAllocInt(1);
        glGenTextures(tex);
    }

    public Texture(int w, int h) {
        this(w, h, GL_RGBA32F, GL_RGBA, GL_FLOAT, GL_LINEAR, false);
    }

    public Texture(int w, int h, boolean useMipmap) {
        this(w, h, GL_RGBA32F, GL_RGBA, GL_FLOAT, GL_LINEAR, useMipmap);
    }

    public Texture(
            int w,
            int h,
            int internalFormat,
            int format,
            int type,
            int filter,
            boolean useMipmap
    ) {
        this();

        this.width = w;
        this.height = h;
        this.useMipmap = useMipmap;

        glBindTexture(GL_TEXTURE_2D, tex.get(0));

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                internalFormat,
                w,
                h,
                0,
                format,
                type,
                0
        );

        applySampling(filter);
        applyWrap(GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_2D, 0);

        uploaded = true;
    }

    public Texture(String path) {
        this(path, true, true);
    }

    public Texture(String path, boolean useMipmap) {
        this(path, true, useMipmap);
    }

    public Texture(String path, boolean flipY, boolean useMipmap) {
        this();

        this.flipYOnUpload = flipY;
        this.useMipmap = useMipmap;

        setTexture(path);
    }

    public Texture setFlipYOnUpload(boolean flipY) {
        this.flipYOnUpload = flipY;
        return this;
    }

    public Texture setUseMipmap(boolean enable) {
        this.useMipmap = enable;

        if (!uploaded || tex == null) {
            return this;
        }

        glBindTexture(GL_TEXTURE_2D, tex.get(0));
        applySampling(GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

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

            if (input != null) {
                try (InputStream in = input) {
                    bytes = in.readAllBytes();
                }
            } else {
                java.io.File file = new java.io.File(path);

                System.out.println("[Texture] Resource not found, try file:");
                System.out.println("Absolute  = " + file.getAbsolutePath());
                System.out.println("Canonical = " + file.getCanonicalPath());
                System.out.println("Exists    = " + file.exists());

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

        applySampling(GL_LINEAR);
        applyWrap(GL_REPEAT);

        glBindTexture(GL_TEXTURE_2D, 0);

        uploaded = true;
    }

    private void applySampling(int filter) {
        if (useMipmap) {
            glGenerateMipmap(GL_TEXTURE_2D);

            if (filter == GL_NEAREST) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            }
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
        }
    }

    private void applyWrap(int mode) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, mode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, mode);
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
        applyWrap(mode);
        glBindTexture(GL_TEXTURE_2D, 0);

        return this;
    }

    public Texture setSamplingMode(int filter) {
        if (tex == null) return this;

        glBindTexture(GL_TEXTURE_2D, tex.get(0));
        applySampling(filter);
        glBindTexture(GL_TEXTURE_2D, 0);

        return this;
    }

    public int getID() {
        if (tex == null) return 0;
        return tex.get(0);
    }

    public int getId() {
        return getID();
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public boolean isUseMipmap() {
        return useMipmap;
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
