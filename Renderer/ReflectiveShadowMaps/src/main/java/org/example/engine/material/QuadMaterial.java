package org.example.engine.material;

import org.example.engine.gl.Texture;

import java.util.Set;

public class QuadMaterial extends Material {

    Texture tex = new Texture(1,1);

    public QuadMaterial(String frag) {
        super(frag);
    }

    public QuadMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public QuadMaterial setTexture(Texture t) {
        tex = t;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (tex != null && tex.isUploaded()) {
            setTexture("tex", tex, 0);
        }
    }

    @Override
    public void cleanup() {
        if (tex != null) {
            unbindTexture(0);
        }
    }

    @Override
    public void collectTextures(Set<Texture> textures) {
        if (tex != null) {
            textures.add(tex);
        }
    }
}
