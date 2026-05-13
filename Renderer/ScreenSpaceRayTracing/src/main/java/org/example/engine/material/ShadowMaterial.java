package org.example.engine.material;

import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;
import org.example.engine.gameobject.GameObject;
import  org.example.engine.light.Light;

public class ShadowMaterial extends Material {
    Light lightSource;

    public ShadowMaterial(String frag) {
        super(frag);
    }

    public ShadowMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public ShadowMaterial setLight(Light l) {
        lightSource = l;
        return this;
    }


    @Override
    public void run(GameObject go, SubMesh subMesh) {
        Matrix4 model = go.localToWorld();
        Matrix4 shadowMatrix = lightSource.getProjectionMatrix().mult(lightSource.getViewMatrix());
        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("shadowMatrix", shadowMatrix);
        setVector3ToUniform("lightPos", lightSource.transform.position);
        setFloatToUniform("lightFar", lightSource.getLightFar());
    }

    @Override
    public void cleanup() {

    }
}
