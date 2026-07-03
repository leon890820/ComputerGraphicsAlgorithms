package org.example.engine.rendererPass;

import org.example.engine.gl.*;
import org.example.engine.light.PointLight;
import org.example.engine.material.PointLightMaterial;
import org.example.engine.render.GBuffer;
import org.example.engine.scene.*;
import org.example.engine.render.RenderContext;

public class PointScenePass extends RenderPass {
    PointLightMaterial pointLightMaterial;
    public PointScenePass(){
        pointLightMaterial = new PointLightMaterial("/shaders/pointLight.frag", "/shaders/quad.vert");
    }

    public void render(RenderContext ctx, GBuffer gBuffer, PointLight light, TextureCube shadowDepth) {
        pointLightMaterial
                .setAlbedoTex(gBuffer.albedo)
                .setNormalTex(gBuffer.normal)
                .setPositionTex(gBuffer.position);
        pointLightMaterial.setDepthTex(shadowDepth);
        pointLightMaterial.setLight(light);
        Camera camera = ctx.camera;
        camera.runWithMaterial(ctx, pointLightMaterial);
    }
}

