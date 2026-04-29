package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.mesh.ObjLoader;

public class Quad extends GameObject {

    public Quad(Material mat) {
        setMesh(new ObjLoader().load("/meshes/quad"));
        buildSubMeshRenderers(mat);
    }
}
