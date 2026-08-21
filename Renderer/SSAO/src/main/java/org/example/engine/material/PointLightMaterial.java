package org.example.engine.material;

import org.example.engine.gl.TextureCube;

public class PointLightMaterial extends LightMaterial{
    TextureCube shadowCubeMap;
    public PointLightMaterial(String frag) {
        super(frag);
    }

    public PointLightMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public LightMaterial setDepthTex(TextureCube t) {
        shadowCubeMap = t;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        setTexture("albedo", albedoTex, 0);
        setTexture("worldNormal", normalTex, 1);
        setTexture("worldPos", positionTex, 2);
        setCubeTexture("shadowCubeMap", shadowCubeMap, 3);
        setIntToUniform("useSSAO", useSSAO ? 1 : 0);
        if (useSSAO) {
            setTexture("ssao", ssaoTex, 4);
        }

        applyLightUniforms(data);
    }
}
