package org.example.engine.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class TextureCube {

    IntBuffer tex;
    int size;

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
        return tex != null && tex.get(0) != 0;
    }

    public void dispose() {
        if (tex != null) {
            glDeleteTextures(tex);
            MemoryUtil.memFree(tex);
            tex = null;
        }
    }
}