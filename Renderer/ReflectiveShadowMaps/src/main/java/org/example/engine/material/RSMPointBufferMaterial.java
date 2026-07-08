package org.example.engine.material;

import org.example.engine.math.Matrix4;

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
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null) {
            return;
        }

        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        setMatrix4ToUniform("lightVPMatrix", shadowMatrix != null ? shadowMatrix : data.shadowMatrix);
        applySkinning(data);
        setVector3ToUniform("lightPos", data.lightPosition);
        setFloatToUniform("lightFar", data.lightFar);
        if (data.baseColorTexture != null && data.baseColorTexture.isUploaded()) {
            setTexture("tex", data.baseColorTexture, 0);
        }
    }
}
