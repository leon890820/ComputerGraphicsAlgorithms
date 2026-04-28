package org.example.engine.rendererPass;

import org.example.engine.gl.*;
import org.example.engine.material.PointLightMaterial;
import org.example.engine.scene.*;
import org.example.engine.render.RenderContext;

public class PointScenePass extends ScenePass{
    PointLightMaterial pointLightMaterial;
    TextureCube shadowCubeMap;
    public PointScenePass(){
        pointLightMaterial = new PointLightMaterial("/shaders/pointLight.frag", "/shaders/quad.vert");
    }

    public void setGBuffer(Texture albedo, Texture normal, Texture position, TextureCube depth) {
        albedoTex = albedo;
        normalTex = normal;
        positionTex = position;
        shadowCubeMap = depth;
    }

    @Override
    public void render(RenderContext ctx) {
        var light = ctx.scene.getLights();
        pointLightMaterial.setAlbedoTex(albedoTex).setNormalTex(normalTex).setPositionTex(positionTex);
        pointLightMaterial.setDepthTex(shadowCubeMap);
        pointLightMaterial.setLight(light.get(0));
        Camera camera = ctx.scene.getCamera();
        camera.runWithMaterial(pointLightMaterial);
    }
}

