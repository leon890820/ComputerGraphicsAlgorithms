package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.*;
import org.example.engine.mesh.SubMesh;

public class ShadingWithPointRSMMaterial extends Material {
    Texture u_AlbedoTexture = new Texture(1,1);
    Texture u_NormalTexture = new Texture(1,1);
    Texture u_PositionTexture = new Texture(1,1);
    TextureCube u_RSMFluxTexture ;
    TextureCube u_RSMNormalTexture ;
    TextureCube u_RSMPositionTexture ;
    TextureCube u_RSMDepthTexture;

    float u_MaxSampleRadius = 100;


    public ShadingWithPointRSMMaterial(String frag) {
        super(frag);
    }

    public ShadingWithPointRSMMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public ShadingWithPointRSMMaterial setAlbedoTexture(Texture texture){
        u_AlbedoTexture = texture;
        return this;
    }

    public ShadingWithPointRSMMaterial setNormalTexture(Texture texture){
        u_NormalTexture = texture;
        return this;
    }

    public ShadingWithPointRSMMaterial setPositionTexture(Texture texture){
        u_PositionTexture = texture;
        return this;
    }

    public ShadingWithPointRSMMaterial setRSMFluxTexture(TextureCube texture){
        u_RSMFluxTexture = texture;
        return this;
    }

    public ShadingWithPointRSMMaterial setRSMNormalTexture(TextureCube texture){
        u_RSMNormalTexture = texture;
        return this;
    }

    public ShadingWithPointRSMMaterial setRSMPositionTexture(TextureCube texture){
        u_RSMPositionTexture = texture;
        return this;
    }

    public ShadingWithPointRSMMaterial setRSMDepthTexture(TextureCube texture){
        u_RSMDepthTexture = texture;
        return this;
    }

    public void run(GameObject go, SubMesh subMesh) {
        setTexture("u_AlbedoTexture", u_AlbedoTexture, 0);
        setTexture("u_NormalTexture", u_NormalTexture, 1);
        setTexture("u_PositionTexture", u_PositionTexture, 2);
        setCubeTexture("u_RSMFluxTexture", u_RSMFluxTexture, 3);
        setCubeTexture("u_RSMNormalTexture", u_RSMNormalTexture, 4);
        setCubeTexture("u_RSMPositionTexture", u_RSMPositionTexture, 5);
        setCubeTexture("u_RSMDepthTexture", u_RSMDepthTexture, 6);


        setFloatToUniform("u_MaxSampleRadius", u_MaxSampleRadius);
        setIntToUniform("u_RSMSize", 1024);
        setIntToUniform("u_VPLNum", 32);
        setVector3ToUniform("u_LightPosInWorldSpace", (lightSource.getPosition()));
        setFloatToUniform("lightFar", lightSource.getLightFar());

        setIntToUniform("RTX", true ? 1 : 0);

    }

    public void cleanup() {
    }
}