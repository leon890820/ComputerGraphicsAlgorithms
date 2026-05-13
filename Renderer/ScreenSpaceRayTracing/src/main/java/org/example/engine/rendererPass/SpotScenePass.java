package org.example.engine.rendererPass;

import org.example.engine.material.LightMaterial;

public class SpotScenePass extends ScenePass{
    public SpotScenePass(){
        lightMaterial = new LightMaterial("/shaders/quad.frag", "/shaders/quad.vert");
    }
}
