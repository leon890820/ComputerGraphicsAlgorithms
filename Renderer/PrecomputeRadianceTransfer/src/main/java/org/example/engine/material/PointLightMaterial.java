package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.TextureCube;
import org.example.engine.mesh.SubMesh;

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
    public void run(GameObject go, SubMesh subMesh) {
        setTexture("albedo", albedoTex, 0);
        setTexture("worldNormal", normalTex, 1);
        setTexture("worldPos", positionTex, 2);
        setCubeTexture("shadowCubeMap", shadowCubeMap, 3);

        lightSource.setShaderParameter(this);
    }
}
