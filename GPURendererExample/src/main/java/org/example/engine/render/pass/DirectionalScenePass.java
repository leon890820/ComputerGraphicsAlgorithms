package org.example.engine.render.pass;

import org.example.engine.material.LightMaterial;

public class DirectionalScenePass extends ScenePass{
    public DirectionalScenePass(){
        lightMaterial = new LightMaterial("/shaders/directionalLight.frag", "/shaders/quad.vert");
    }
}
