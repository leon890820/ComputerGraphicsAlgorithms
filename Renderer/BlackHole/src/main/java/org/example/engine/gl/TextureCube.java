package org.example.engine.gl;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class TextureCube {

    IntBuffer tex;
    int size;
    boolean uploaded = false;

    public TextureCube(int size, int internalFormat, int format, int type, int filter) {
        this.size = size;

        tex = MemoryUtil.memAllocInt(1);

        glGenTextures(tex);
        glBindTexture(GL_TEXTURE_CUBE_MAP, tex.get(0));

        for (int i = 0; i < 6; i++) {
            glTexImage2D(
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    0,
                    internalFormat,
                    size,
                    size,
                    0,
                    format,
                    type,
                    0
            );
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, filter);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        uploaded = true;
    }

    public TextureCube(String basePath) {
        this(resolveFaces(basePath));
    }

    private static String[] resolveFaces(String basePath) {
        String[] names = new String[]{"posx", "negx", "posy", "negy", "posz", "negz"};
        String[] faces = new String[6];

        for (int i = 0; i < names.length; i++) {
            String png = basePath + "/" + names[i] + ".png";
            String jpg = basePath + "/" + names[i] + ".jpg";
            faces[i] = resourceExists(png) || fileExists(png) ? png : jpg;
        }

        return faces;
    }

    private static boolean resourceExists(String path) {
        InputStream input = TextureCube.class.getResourceAsStream(path);
        if (input == null && !path.startsWith("/")) {
            input = TextureCube.class.getResourceAsStream("/" + path);
        }

        if (input == null) {
            return false;
        }

        try {
            input.close();
        } catch (Exception ignored) {
        }

        return true;
    }

    private static boolean fileExists(String path) {
        return new java.io.File(path).exists();
    }

    public TextureCube(String[] faces) {
        if (faces == null || faces.length != 6) {
            throw new IllegalArgumentException("[TextureCube] cubemap needs exactly 6 face paths.");
        }

        tex = MemoryUtil.memAllocInt(1);
        glGenTextures(tex);
        glBindTexture(GL_TEXTURE_CUBE_MAP, tex.get(0));

        STBImage.stbi_set_flip_vertically_on_load(false);

        for (int i = 0; i < faces.length; i++) {
            uploadFace(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, faces[i]);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        uploaded = true;
    }

    private void uploadFace(int target, String path) {
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

            ByteBuffer image = STBImage.stbi_load_from_memory(encoded, w, h, channels, 4);
            if (image == null) {
                throw new RuntimeException("[TextureCube] STB failed: " + STBImage.stbi_failure_reason());
            }

            glTexImage2D(
                    target,
                    0,
                    GL_RGBA8,
                    w.get(0),
                    h.get(0),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    image
            );

            if (size == 0) {
                size = w.get(0);
            }

            STBImage.stbi_image_free(image);
        } catch (Exception e) {
            throw new RuntimeException("[TextureCube] Failed to load cubemap face: " + path, e);
        } finally {
            if (encoded != null) MemoryUtil.memFree(encoded);
            if (w != null) MemoryUtil.memFree(w);
            if (h != null) MemoryUtil.memFree(h);
            if (channels != null) MemoryUtil.memFree(channels);
        }
    }

    private byte[] readImageBytes(String path) throws Exception {
        InputStream input = TextureCube.class.getResourceAsStream(path);

        if (input == null && !path.startsWith("/")) {
            input = TextureCube.class.getResourceAsStream("/" + path);
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

    public void bind(int unit) {
        if (tex == null) return;

        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_CUBE_MAP, tex.get(0));
    }

    public void unbind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    public int getID() {
        if (tex == null) return 0;
        return tex.get(0);
    }

    public int getId() {
        return getID();
    }

    public boolean isUploaded() {
        return uploaded && tex != null && tex.get(0) != 0;
    }

    public void dispose() {
        if (tex != null) {
            glDeleteTextures(tex);
            MemoryUtil.memFree(tex);
            tex = null;
        }
        uploaded = false;
    }
}
