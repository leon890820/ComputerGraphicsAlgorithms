package org.example.engine.rendererPass;

import org.example.engine.gl.Texture;
import org.example.engine.material.LightMaterial;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

public class ScenePass extends RenderPass {
    LightMaterial lightMaterial;
    Texture albedoTex;
    Texture normalTex;
    Texture positionTex;
    Texture depthTex;

    public ScenePass(){
        lightMaterial = new LightMaterial("/shaders/quad.frag", "/shaders/quad.vert");
    }

    public void setGBuffer(Texture albedo, Texture normal, Texture position, Texture depth) {
        albedoTex = albedo;
        normalTex = normal;
        positionTex = position;
        depthTex = depth;
    }


    @Override
    public void render(RenderContext ctx) {
        var light = ctx.scene.getLights();
        lightMaterial.setAlbedoTex(albedoTex);
        lightMaterial.setLight(light.get(0));
        Camera camera = ctx.scene.getCamera();
        camera.runWithMaterial(lightMaterial);
    }
}
