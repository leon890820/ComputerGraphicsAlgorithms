package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;

public class PointShadowMaterial extends ShadowMaterial {
    Matrix4 shadowMatrix;

    public PointShadowMaterial(String frag) {
        super(frag);
    }

    public PointShadowMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public PointShadowMaterial setShadowMatrix(Matrix4 m){
        shadowMatrix = m;
        return this;
    }

    @Override
    public void run(GameObject go, SubMesh subMesh) {
        Matrix4 model = go.localToWorld();
        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("shadowMatrix", shadowMatrix);
        setVector3ToUniform("lightPos", lightSource.transform.position);
        setFloatToUniform("lightFar", lightSource.getLightFar());

    }
}