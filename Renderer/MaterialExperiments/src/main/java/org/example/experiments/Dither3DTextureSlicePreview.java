package org.example.experiments;

import org.example.engine.component.core.Component;
import org.example.engine.component.render.MeshRenderer;
import org.example.engine.gameobject.Quad;
import org.example.engine.gl.Texture3D;
import org.example.engine.gl.Texture3DData;
import org.example.engine.material.VolumeSliceMaterial;
import org.example.engine.render.RenderContext;

import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;

public class Dither3DTextureSlicePreview extends Component {
    private static final int MARGIN = 16;
    private static final int MAX_PREVIEW_SIZE = 320;

    private final Path cachePath;

    private Quad quad;
    private Texture3D texture;
    private VolumeSliceMaterial material;
    private float elapsedTime;

    public Dither3DTextureSlicePreview(Path cachePath) {
        this.cachePath = cachePath;
    }

    @Override
    public void start() {
        try {
            Texture3DData data = Dither3DTextureFactory.loadOrCreateBayer8x8(cachePath);
            texture = data.uploadNearest().setWrapMode(GL_REPEAT);
            material = new VolumeSliceMaterial("/shaders/quad.frag", "/shaders/quad.vert")
                    .setVolumeTexture(texture);
            quad = new Quad(material);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Dither3D 8x8 texture preview: " + cachePath, e);
        }
    }

    @Override
    public void update(float deltaTime) {
        elapsedTime += deltaTime;
    }

    @Override
    public void render(RenderContext ctx) {
        if (ctx == null || quad == null || material == null) {
            return;
        }

        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        boolean depthTestWasEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean blendWasEnabled = glIsEnabled(GL_BLEND);

        int previewSize = Math.min(MAX_PREVIEW_SIZE, Math.max(128, Math.min(ctx.screenWidth, ctx.screenHeight) / 3));
        glViewport(MARGIN, MARGIN, previewSize, previewSize);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        float sliceDepth = 0.5f + 0.5f * (float) Math.sin(elapsedTime * 0.65f);
        material.setSliceDepth(sliceDepth);

        for (MeshRenderer renderer : quad.getMeshRenderers()) {
            renderer.render(ctx, material);
        }

        glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        if (depthTestWasEnabled) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        if (blendWasEnabled) {
            glEnable(GL_BLEND);
        } else {
            glDisable(GL_BLEND);
        }
    }

    @Override
    public void dispose() {
        if (quad != null) {
            quad.dispose();
            quad = null;
            material = null;
        }

        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}

