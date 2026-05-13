package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.mesh.SubMesh;

public class SSRMaterial extends Material{
    public SSRMaterial(String frag) {
        super(frag);
    }

    public SSRMaterial(String frag, String vert) {
        super(frag, vert);
    }
    @Override
    public void run(GameObject go, SubMesh subMesh) {


    }
}
