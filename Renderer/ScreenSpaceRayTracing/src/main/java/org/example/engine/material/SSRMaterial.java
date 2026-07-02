package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;


public class SSRMaterial extends Material{

    Texture albedoTex;
    Texture normalTex;
    Texture worldPosTex;
    Texture depthTex;

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

    @Override
    public void run(GameObject go, SubMesh subMesh) {
        Matrix4 model = go.localToWorld();
        var camera = go.scene.getCamera();
        Matrix4 V = camera.getViewMatrix();
        Matrix4 P = camera.getProjectionMatrix();

        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("u_ViewMatrix", V);
        setMatrix4ToUniform("u_ProjectionMatrix", P);

        setTexture("albedoTex", albedoTex, 0);
        setTexture("normalTex", normalTex, 1);
        setTexture("worldPosTex", worldPosTex, 2);
        setTexture("depthTex", depthTex, 3);

        setVector3ToUniform("cameraPos", camera.transform.position);
        setFloatToUniform("cameraFar", camera.getFar());
        setFloatToUniform("u_WindowWidth", 1024);
        setFloatToUniform("u_WindowHeight", 1024);
    }
}
