package org.example.engine.material;

import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;
import org.example.engine.gameobject.GameObject;
import  org.example.engine.light.Light;

public class ShadowMaterial extends Material {
    private static final int MAX_BONES = 100;

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
        applySkinningUniforms(go, subMesh);
    }

    @Override
    public void cleanup() {

    }

    protected void applySkinningUniforms(GameObject go, SubMesh subMesh) {
        Matrix4[] boneMatrices = go.getBoneMatricesForSubMesh(subMesh);
        boolean useSkinning = boneMatrices != null && boneMatrices.length > 0;
        setIntToUniform("useSkinning", useSkinning ? 1 : 0);
        if (useSkinning) {
            setMatrix4ArrayToUniform("boneMatrices[0]", boneMatrices, MAX_BONES);
        }
    }
}
