package org.example.engine.rendererPass;

import org.example.engine.gl.Texture;
import org.example.engine.material.SceneMaterial;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

public class ScenePass extends RenderPass {
    SceneMaterial sceneMaterial;
    Texture albedoTex;
    public ScenePass(){
        sceneMaterial = new SceneMaterial("/shaders/quad.frag", "/shaders/quad.vert");
    }

    public void setGBuffer(Texture albedo) {
        albedoTex = albedo;
    }


    @Override
    public void render(RenderContext ctx) {
        var light = ctx.scene.getLights();
        sceneMaterial.setAlbedoTex(albedoTex);
        sceneMaterial.setLight(light.get(0));
        Camera camera = ctx.scene.getCamera();
        camera.runWithMaterial(sceneMaterial);
    }
}
