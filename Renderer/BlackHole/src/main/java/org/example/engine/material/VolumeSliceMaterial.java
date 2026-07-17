package org.example.engine.material;

import org.example.engine.gl.Texture3D;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

public class VolumeSliceMaterial extends Material {

    private Texture3D volumeTexture;
    private float sliceDepth;

    public VolumeSliceMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public VolumeSliceMaterial setVolumeTexture(Texture3D volumeTexture) {
        this.volumeTexture = volumeTexture;
        return this;
    }

    public VolumeSliceMaterial setSliceDepth(float sliceDepth) {
        this.sliceDepth = sliceDepth;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (volumeTexture == null || !volumeTexture.isUploaded()) {
            System.out.println("[VolumeSliceMaterial] Warning: volumeTexture is null or not uploaded");
            return;
        }

        volumeTexture.bind(0);
        setIntToUniform("volumeTexture", 0);
        setFloatToUniform("sliceDepth", sliceDepth);
    }

    @Override
    public void cleanup() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}