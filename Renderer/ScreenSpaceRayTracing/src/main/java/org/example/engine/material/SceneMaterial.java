package org.example.engine.material;

import org.example.engine.gl.Texture;

public class SceneMaterial extends Material {

    Texture albedoTex = new Texture(1,1);

    public SceneMaterial(String frag) {
        super(frag);
    }

    public SceneMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public SceneMaterial setAlbedoTex(Texture t) {
        albedoTex = t;
        return this;
    }


    @Override
    public void run(MaterialRenderData data) {
        setTexture("albedo", albedoTex, 0);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
