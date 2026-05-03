package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;

public class RSMPointBufferMaterial extends RSMBufferMaterial {
    Matrix4 shadowMatrix;

    public RSMPointBufferMaterial(String frag) {
        super(frag);
    }

    public RSMPointBufferMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public RSMPointBufferMaterial setShadowMatrix(Matrix4 m){
        shadowMatrix = m;
        return this;
    }

    @Override
    public void run(GameObject go, SubMesh subMesh) {
        Matrix4 model = go.localToWorld();
        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("lightVPMatrix", shadowMatrix);
        setVector3ToUniform("lightPos", lightSource.transform.position);
        setFloatToUniform("lightFar", lightSource.getLightFar());
        if (subMesh != null && subMesh.textureKa != null && subMesh.textureKa.isUploaded()) {
            setTexture("tex", subMesh.textureKa, 0);
        }
    }
}
