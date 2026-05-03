package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.gameobject.GameObject;
import org.example.engine.mesh.SubMesh;

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
    public void run(GameObject go, SubMesh subMesh) {
        setTexture("albedo", albedoTex, 0);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
