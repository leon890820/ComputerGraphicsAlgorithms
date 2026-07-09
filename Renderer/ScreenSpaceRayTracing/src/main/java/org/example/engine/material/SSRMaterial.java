package org.example.engine.material;

import org.example.engine.gl.Texture;

import java.util.Set;


public class SSRMaterial extends Material{

    Texture albedoTex;
    Texture normalTex;
    Texture worldPosTex;
    Texture depthTex;
    Texture normalMapTex;
    float fuzz = 0.0f;
    int fuzzySampleCount = 1;
    public SSRMaterial(String frag) {
        super(frag);
    }

    public SSRMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public SSRMaterial setAlbedoTex(Texture t) {
        albedoTex = t;
        return this;
    }
    public SSRMaterial setNormalTex(Texture t) {
        normalTex = t;
        return this;
    }
    public SSRMaterial setWorldPosTex(Texture t) {
        worldPosTex = t;
        return this;
    }
    public SSRMaterial setDepthTex(Texture t) {
        depthTex = t;
        return this;
    }

    public SSRMaterial setNormalMap(Texture t) {
        normalMapTex = t;
        return this;
    }

    public Texture getNormalMap() {
        return normalMapTex;
    }

    public SSRMaterial setFuzz(float fuzz) {
        this.fuzz = Math.max(0.0f, fuzz);
        return this;
    }

    public SSRMaterial setFuzzySampleCount(int sampleCount) {
        fuzzySampleCount = Math.max(1, sampleCount);
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.viewMatrix == null || data.projectionMatrix == null) {
            System.out.println("[SSRMaterial] render data is missing camera or model matrix.");
            return;
        }

        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        setMatrix4ToUniform("u_ViewMatrix", data.viewMatrix);
        setMatrix4ToUniform("u_ProjectionMatrix", data.projectionMatrix);

        setTexture("albedoTex", albedoTex, 0);
        setTexture("normalTex", normalTex, 1);
        setTexture("worldPosTex", worldPosTex, 2);
        setTexture("depthTex", depthTex, 3);
        if (normalMapTex != null && normalMapTex.isUploaded()) {
            setIntToUniform("hasNormalMap", 1);
            setTexture("floorNormalMap", normalMapTex, 4);
        } else {
            setIntToUniform("hasNormalMap", 0);
        }

        setVector3ToUniform("cameraPos", data.viewPosition);
        setFloatToUniform("cameraFar", data.cameraFar);
        setFloatToUniform("u_WindowWidth", data.screenWidth);
        setFloatToUniform("u_WindowHeight", data.screenHeight);
        setFloatToUniform("u_Fuzz", fuzz);
        setIntToUniform("u_FuzzySampleCount", fuzzySampleCount);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        unbindTexture(1);
        unbindTexture(2);
        unbindTexture(3);
        unbindTexture(4);
    }

    @Override
    public void collectTextures(Set<Texture> textures) {
        if (normalMapTex != null) {
            textures.add(normalMapTex);
        }
    }
}
