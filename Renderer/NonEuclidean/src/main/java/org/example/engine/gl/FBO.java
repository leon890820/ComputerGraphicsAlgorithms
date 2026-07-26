package org.example.engine.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

public class FBO {

    IntBuffer fbo;
    public IntBuffer rbo;

    public Texture[] tex;
    public Texture depthTex;

    int fboWidth;
    int fboHeight;
    int colorCount;

    public FBO(int w, int h, int num, int filter, boolean useDepthTexture) {

        fboWidth = w;
        fboHeight = h;
        colorCount = num;

        fbo = MemoryUtil.memAllocInt(1);
        rbo = MemoryUtil.memAllocInt(1);
        tex = new Texture[num];

        glGenFramebuffers(fbo);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.get(0));

        int[] attachments = new int[num];

        // ===== Color attachments =====
        for (int i = 0; i < num; i++) {

            tex[i] = new Texture(w, h);

            glBindTexture(GL_TEXTURE_2D, tex[i].getID());

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

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_COLOR_ATTACHMENT0 + i,
                    GL_TEXTURE_2D,
                    tex[i].getID(),
                    0
            );

            attachments[i] = GL_COLOR_ATTACHMENT0 + i;
        }

        glBindTexture(GL_TEXTURE_2D, 0);

        // ===== Depth =====
        if (useDepthTexture) {

            depthTex = new Texture(w, h);

            glBindTexture(GL_TEXTURE_2D, depthTex.getID());

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_DEPTH_COMPONENT24,
                    w,
                    h,
                    0,
                    GL_DEPTH_COMPONENT,
                    GL_FLOAT,
                    0
            );

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glFramebufferTexture2D(
                    GL_FRAMEBUFFER,
                    GL_DEPTH_ATTACHMENT,
                    GL_TEXTURE_2D,
                    depthTex.getID(),
                    0
            );

            glBindTexture(GL_TEXTURE_2D, 0);

        } else {

            glGenRenderbuffers(rbo);
            glBindRenderbuffer(GL_RENDERBUFFER, rbo.get(0));

            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, w, h);

            glFramebufferRenderbuffer(
                    GL_FRAMEBUFFER,
                    GL_DEPTH_ATTACHMENT,
                    GL_RENDERBUFFER,
                    rbo.get(0)
            );

            glBindRenderbuffer(GL_RENDERBUFFER, 0);
        }

        glDrawBuffers(attachments);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);

        if (status != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("[FBO] Framebuffer incomplete! status = " + status);
        } else {
            System.out.println("[FBO] OK " + w + "x" + h + " attachments=" + num);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bindFrameBuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo.get(0));
        glViewport(0, 0, fboWidth, fboHeight);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void unbindFrameBuffer(int screenW, int screenH) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, screenW, screenH);
    }

    public Texture getColorTexture(int index) {
        if (index < 0 || index >= colorCount) {
            System.out.println("[FBO] invalid color index: " + index);
            return null;
        }
        return tex[index];
    }

    public Texture getDepthTexture() {
        return depthTex;
    }

    void dispose() {

        if (tex != null) {
            for (Texture t : tex) {
                if (t != null) t.dispose();
            }
        }

        if (depthTex != null) {
            depthTex.dispose();
        }

        if (rbo != null) {
            glDeleteRenderbuffers(rbo);
            MemoryUtil.memFree(rbo);
        }

        if (fbo != null) {
            glDeleteFramebuffers(fbo);
            MemoryUtil.memFree(fbo);
        }
    }
}