package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;
import org.example.engine.scene.Camera;

public class ShadingWithRSMMaterial extends Material {
    Texture u_AlbedoTexture = new Texture(1,1);
    Texture u_NormalTexture = new Texture(1,1);
    Texture u_PositionTexture = new Texture(1,1);
    Texture u_RSMFluxTexture = new Texture(1,1);
    Texture u_RSMNormalTexture = new Texture(1,1);
    Texture u_RSMPositionTexture = new Texture(1,1);
    Texture u_RSMDepthTexture = new Texture(1,1);

    float u_MaxSampleRadius = 100;


    public ShadingWithRSMMaterial(String frag) {
        super(frag);
    }

    public ShadingWithRSMMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public ShadingWithRSMMaterial setAlbedoTexture(Texture texture){
        u_AlbedoTexture = texture;
        return this;
    }

    public ShadingWithRSMMaterial setNormalTexture(Texture texture){
        u_NormalTexture = texture;
        return this;
    }

    public ShadingWithRSMMaterial setPositionTexture(Texture texture){
        u_PositionTexture = texture;
        return this;
    }

    public ShadingWithRSMMaterial setRSMFluxTexture(Texture texture){
        u_RSMFluxTexture = texture;
        return this;
    }

    public ShadingWithRSMMaterial setRSMNormalTexture(Texture texture){
        u_RSMNormalTexture = texture;
        return this;
    }

    public ShadingWithRSMMaterial setRSMPositionTexture(Texture texture){
        u_RSMPositionTexture = texture;
        return this;
    }

    public ShadingWithRSMMaterial setRSMDepthTexture(Texture texture){
        u_RSMDepthTexture = texture;
        return this;
    }

    public void run(GameObject go, SubMesh subMesh) {
        setTexture("u_AlbedoTexture", u_AlbedoTexture, 0);
        setTexture("u_NormalTexture", u_NormalTexture, 1);
        setTexture("u_PositionTexture", u_PositionTexture, 2);
        setTexture("u_RSMFluxTexture", u_RSMFluxTexture, 3);
        setTexture("u_RSMNormalTexture", u_RSMNormalTexture, 4);
        setTexture("u_RSMPositionTexture", u_RSMPositionTexture, 5);
        setTexture("u_RSMDepthTexture", u_RSMDepthTexture, 6);

        var camera = (Camera) go;

        Matrix4 lightView = lightSource.getViewMatrix();
        Matrix4 lightProject = lightSource.getProjectionMatrix();
        Matrix4 view = camera.getViewMatrix();

        setMatrix4ToUniform("u_LightVPMatrix", lightProject.mult(lightView));
        setFloatToUniform("u_MaxSampleRadius", u_MaxSampleRadius);
        setIntToUniform("u_RSMSize", 1024);
        setIntToUniform("u_VPLNum", 32);
        setVector3ToUniform("u_LightDirInWorldSpace", (lightSource.getLightDir()));
        setVector3ToUniform("u_LightPosInWorldSpace", (lightSource.getPosition()));
        setFloatToUniform("lightFar", lightSource.getLightFar());

        setIntToUniform("RTX", true ? 1 : 0);

    }

    public void cleanup() {
    }
}