package org.example.engine.material;

import org.example.engine.gl.Texture;

public class LightMaterial extends Material {

    Texture albedoTex = new Texture(1,1);
    Texture normalTex = new Texture(1,1);
    Texture positionTex = new Texture(1,1);
    Texture depthTex = new Texture(1,1);
    Texture ssaoTex = new Texture(1,1);
    boolean useSSAO = false;

    public LightMaterial(String frag) {
        super(frag);
    }

    public LightMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public LightMaterial setAlbedoTex(Texture t) {
        albedoTex = t;
        return this;
    }
    public LightMaterial setNormalTex(Texture t) {
        normalTex = t;
        return this;
    }
    public LightMaterial setPositionTex(Texture t) {
        positionTex = t;
        return this;
    }
    public LightMaterial setDepthTex(Texture t) {
        depthTex = t;
        return this;
    }

    public LightMaterial setSSAOTex(Texture t) {
        ssaoTex = t;
        useSSAO = t != null;
        return this;
    }


    @Override
    public void run(MaterialRenderData data) {
        setTexture("albedo", albedoTex, 0);
        setTexture("worldNormal", normalTex, 1);
        setTexture("worldPos", positionTex, 2);
        setTexture("shadowMap", depthTex, 3);
        setIntToUniform("useSSAO", useSSAO ? 1 : 0);
        if (useSSAO) {
            setTexture("ssao", ssaoTex, 4);
        }

        applyLightUniforms(data);

    }

    protected void applyLightUniforms(MaterialRenderData data) {
        if (data == null || !data.hasLight) {
            return;
        }

        setVector3ToUniform("light_color", data.lightColor);
        setVector3ToUniform("light_dir", data.lightDirection);
        setVector3ToUniform("light_pos", data.lightPosition);
        setFloatToUniform("lightFar", data.lightFar);

        if (data.lightSpaceMatrix != null) {
            setMatrix4ToUniform("lightSpaceMatrix", data.lightSpaceMatrix);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        unbindTexture(1);
        unbindTexture(2);
        unbindTexture(3); // shadowMap
        unbindTexture(4);
    }
}
