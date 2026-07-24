package org.example.engine.render.pass;

import org.example.engine.gl.ComputeHelper;
import org.example.engine.gl.ComputeShader;
import org.example.engine.gl.Texture3D;
import org.example.engine.component.render.MeshRenderer;
import org.example.engine.material.VolumeSliceMaterial;
import org.example.engine.render.RenderContext;

public class ComputeExamplePass extends RenderPass {
    private static final int VOLUME_SIZE = 64;

    private final ComputeShader computeShader;
    private final Texture3D volumeTexture;
    private final VolumeSliceMaterial sliceMaterial;

    private float time;

    public ComputeExamplePass(int width, int height) {
        computeShader = new ComputeShader("/shaders/sceneD_volume.comp");
        volumeTexture = ComputeHelper.createVolumeTexture(VOLUME_SIZE);
        sliceMaterial = new VolumeSliceMaterial(
                "/shaders/quad.frag",
                "/shaders/quad.vert"
        ).setVolumeTexture(volumeTexture);
    }

    public void render(RenderContext ctx) {
        computeShader.bind();
        ComputeHelper.assignImage(computeShader, volumeTexture, "volumeImage", 0);
        computeShader.setVector3("volumeSize", volumeTexture.getWidth(), volumeTexture.getHeight(), volumeTexture.getDepth());
        computeShader.setFloat("time", time);
        ComputeHelper.dispatch(computeShader, volumeTexture);
        ComputeHelper.memoryBarrier();
        computeShader.unbind();

        float sliceDepth = 0.5f + 0.5f * (float) Math.sin(time * 0.7f);
        sliceMaterial.setSliceDepth(sliceDepth);
        for (MeshRenderer renderer : ctx.camera.getMeshRenderers()) {
            renderer.render(ctx, sliceMaterial);
        }

        time += 0.016f;
    }

    public void dispose() {
        computeShader.dispose();
        volumeTexture.dispose();
        sliceMaterial.dispose();
    }
}
