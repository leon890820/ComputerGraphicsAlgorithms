package org.example.engine.rendererPass;
import org.example.engine.gl.Texture;
import org.example.engine.material.*;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

abstract public class ScenePass extends RenderPass {
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
        sceneMaterial.setAlbedoTex(albedoTex);
        Camera camera = ctx.scene.getCamera();
        camera.runWithMaterial(sceneMaterial);
    }
}
