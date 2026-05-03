package org.example.engine.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class CubeMapFBO {

    IntBuffer fbo;

    public TextureCube[] colorTex;
    public TextureCube depthTex;

    int size;
    int colorCount;

    public CubeMapFBO(int size, int colorCount, boolean useDepthTexture) {

        this.size = size;
        this.colorCount = colorCount;

        fbo = MemoryUtil.memAllocInt(1);
        glGenFramebuffers(fbo);

        colorTex = new TextureCube[colorCount];

        for (int i = 0; i < colorCount; i++) {
            colorTex[i] = new TextureCube(
                    size,
                    GL_RGBA32F,
                    GL_RGBA,
                    GL_FLOAT,
                    GL_LINEAR
            );
        }

        if (useDepthTexture) {
            depthTex = new TextureCube(
                    size,
                    GL_DEPTH_COMPONENT24,
                    GL_DEPTH_COMPONENT,
                    GL_FLOAT,
                    GL_NEAREST
            );
        }
    }

    public void bindFace(int faceIndex) {

        glBindFramebuffer(GL_FRAMEBUFFER, fbo.get(0));
        glViewport(0, 0, size, size);

        int[] drawBuffers = new int[colorCount];

        for (int i = 0; i < colorCount; i++) {

            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_COLOR_ATTACHMENT0 + i,
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + faceIndex,
                    colorTex[i].getID(),
                    0
            );

            drawBuffers[i] = GL_COLOR_ATTACHMENT0 + i;
        }

        if (depthTex != null) {
            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_DEPTH_ATTACHMENT,
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + faceIndex,
                    depthTex.getID(),
                    0
            );
        }

        if (colorCount > 0) {
            glDrawBuffers(drawBuffers);
        } else {
            glDrawBuffer(GL_NONE);
            glReadBuffer(GL_NONE);
        }

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("[CubeMapFBO] incomplete face=" + faceIndex);
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void unbind(int screenW, int screenH) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, screenW, screenH);
    }

    public TextureCube getColorTexture(int index) {
        if (index < 0 || index >= colorCount) {
            System.out.println("[CubeMapFBO] invalid index: " + index);
            return null;
        }
        return colorTex[index];
    }

    public TextureCube getDepthTexture() {
        return depthTex;
    }

    public void dispose() {

        if (colorTex != null) {
            for (TextureCube t : colorTex) {
                if (t != null) t.dispose();
            }
        }

        if (depthTex != null) {
            depthTex.dispose();
        }

        if (fbo != null) {
            glDeleteFramebuffers(fbo);
            MemoryUtil.memFree(fbo);
            fbo = null;
        }
    }
}