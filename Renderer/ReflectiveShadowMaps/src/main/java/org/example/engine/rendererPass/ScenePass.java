package org.example.engine.rendererPass;

import org.example.engine.gl.Texture;
import org.example.engine.light.Light;
import org.example.engine.material.SceneMaterial;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

public class ScenePass extends RenderPass {
    SceneMaterial sceneMaterial;
    Texture albedoTex;
    public ScenePass(){
        sceneMaterial = new SceneMaterial("/shaders/quad.frag", "/shaders/quad.vert");
    }

    public void render(RenderContext ctx, Texture albedo, Light light) {
        albedoTex = albedo;
        sceneMaterial.setAlbedoTex(albedoTex);
        sceneMaterial.setLight(light);
        Camera camera = ctx.camera;
        camera.runWithMaterial(ctx, sceneMaterial);
    }
}
