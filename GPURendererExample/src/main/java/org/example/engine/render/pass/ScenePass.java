package org.example.engine.render.pass;
import org.example.engine.gl.Texture;
import org.example.engine.light.Light;
import org.example.engine.material.LightMaterial;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

abstract public class ScenePass extends RenderPass {
    LightMaterial lightMaterial;

    public ScenePass(){
        //lightMaterial = new LightMaterial("Shaders/spotLight.frag", "Shaders/quad.vert");
    }

    public void render(RenderContext ctx, GBuffer gBuffer, Light light, Texture shadowDepth) {
        lightMaterial
                .setAlbedoTex(gBuffer.albedo)
                .setNormalTex(gBuffer.normal)
                .setPositionTex(gBuffer.position)
                .setDepthTex(shadowDepth);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            Camera camera = ctx.camera;
            camera.runWithMaterial(ctx, lightMaterial);
        } finally {
            ctx.activeLight = previousLight;
        }
    }
}
