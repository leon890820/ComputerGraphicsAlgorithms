package org.example.engine.render.pass;
import org.example.engine.component.render.MeshRenderer;
import org.example.engine.gl.Texture;
import org.example.engine.light.Light;
import org.example.engine.material.LightMaterial;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;

public class ScenePass extends RenderPass {
    LightMaterial lightMaterial;

    public ScenePass(String fragmentShader, String vertexShader) {
        lightMaterial = new LightMaterial(fragmentShader, vertexShader);
    }

    public void render(RenderContext ctx, GBuffer gBuffer, Light light, Texture shadowDepth) {
        render(ctx, gBuffer, light, shadowDepth, null);
    }

    public void render(RenderContext ctx, GBuffer gBuffer, Light light, Texture shadowDepth, Texture ssaoTexture) {
        lightMaterial
                .setAlbedoTex(gBuffer.albedo)
                .setNormalTex(gBuffer.normal)
                .setPositionTex(gBuffer.position)
                .setDepthTex(shadowDepth)
                .setSSAOTex(ssaoTexture);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            for (MeshRenderer renderer : ctx.camera.getMeshRenderers()) {
                renderer.render(ctx, lightMaterial);
            }
        } finally {
            ctx.activeLight = previousLight;
        }
    }
}
