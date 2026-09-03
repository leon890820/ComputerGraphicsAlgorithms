package org.example.engine.material;

import org.example.engine.math.Matrix4;

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
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null) {
            return;
        }

        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        setMatrix4ToUniform("shadowMatrix", shadowMatrix);
        setVector3ToUniform("lightPos", data.lightPosition);
        setFloatToUniform("lightFar", data.lightFar);
        applySkinningUniforms(data.boneMatrices);

    }
}
