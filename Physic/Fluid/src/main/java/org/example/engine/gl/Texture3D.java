package org.example.engine.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Texture3D {
    private IntBuffer tex;
    private int width;
    private int height;
    private int depth;
    private boolean uploaded;

    public Texture3D(int width, int height, int depth, int internalFormat, int format, int type, int filter) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        tex = MemoryUtil.memAllocInt(1);
        glGenTextures(tex);

        glBindTexture(GL_TEXTURE_3D, tex.get(0));
        glTexImage3D(
                GL_TEXTURE_3D,
                0,
                internalFormat,
                width,
                height,
                depth,
                0,
                format,
                type,
                0
        );

        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, filter);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_3D, 0);

        uploaded = true;
    }

    public void bind(int unit) {
        if (tex == null) return;

        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_3D, tex.get(0));
    }

    public void unbind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_3D, 0);
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
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
        depth = 0;
    }
}
