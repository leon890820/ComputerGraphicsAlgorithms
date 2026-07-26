package org.example.engine.render.pass;

import org.example.engine.gl.*;
import org.example.engine.component.render.MeshRenderer;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.material.PointLightMaterial;
import org.example.engine.render.GBuffer;
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
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            for (MeshRenderer renderer : ctx.camera.getMeshRenderers()) {
                renderer.render(ctx, pointLightMaterial);
            }
        } finally {
            ctx.activeLight = previousLight;
        }
    }
}

