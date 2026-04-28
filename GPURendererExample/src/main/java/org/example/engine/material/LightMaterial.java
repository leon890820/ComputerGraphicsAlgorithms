package org.example.engine.material;

import org.example.engine.material.Material;
import org.example.engine.gl.Texture;
import org.example.engine.gameobject.GameObject;
import org.example.engine.mesh.SubMesh;
import org.example.engine.light.Light;

public class LightMaterial extends Material {

    Texture albedoTex = new Texture(1,1);
    Texture normalTex = new Texture(1,1);
    Texture positionTex = new Texture(1,1);
    Texture depthTex = new Texture(1,1);

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


    @Override
    public void run(GameObject go, SubMesh subMesh) {
        setTexture("albedo", albedoTex, 0);
        setTexture("worldNormal", normalTex, 1);
        setTexture("worldPos", positionTex, 2);
        setTexture("shadowMap", depthTex, 3);

        lightSource.setShaderParameter(this);

    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        unbindTexture(1);
        unbindTexture(2);
        unbindTexture(4); // shadowMap 如果有綁
    }
}
