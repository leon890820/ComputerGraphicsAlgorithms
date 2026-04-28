package org.example.engine.gameobject;

import org.example.engine.mesh.ObjLoader;
import org.example.engine.material.Material;


public class PhongObject extends GameObject {

    public PhongObject() {
    }

    public PhongObject(String name, Material mat) {
        setMesh(new ObjLoader().load(name));
        buildSubMeshRenderers(mat);
    }
}