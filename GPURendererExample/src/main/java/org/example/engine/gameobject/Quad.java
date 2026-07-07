package org.example.engine.gameobject;

import org.example.engine.material.Material;

public class Quad extends MeshObject {

    public Quad(Material mat) {
        load("/meshes/quad", mat);
    }
}
