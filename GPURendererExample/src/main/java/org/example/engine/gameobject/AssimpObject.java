package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.mesh.AssimpLoader;

public class AssimpObject extends GameObject {

    public AssimpObject() {
    }

    public AssimpObject(String path, Material mat) {
        setMesh(new AssimpLoader().load(path));
        buildSubMeshRenderers(mat);
    }
}
