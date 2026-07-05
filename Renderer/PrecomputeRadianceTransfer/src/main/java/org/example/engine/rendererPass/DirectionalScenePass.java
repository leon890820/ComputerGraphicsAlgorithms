package org.example.engine.rendererPass;

import org.example.engine.material.LightMaterial;

public class DirectionalScenePass extends ScenePass{
    public DirectionalScenePass(){
        lightMaterial = new LightMaterial("/shaders/directionalLight.frag", "/shaders/quad.vert");
    }
}
