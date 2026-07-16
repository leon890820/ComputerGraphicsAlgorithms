package org.example.engine.material;

import org.example.engine.gl.Texture;

public class TexturePreviewMaterial extends Material {

    private Texture texture;

    public TexturePreviewMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public TexturePreviewMaterial setTexture(Texture texture) {
        this.texture = texture;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        setTexture("screenTexture", texture, 0);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
