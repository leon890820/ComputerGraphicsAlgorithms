package org.example.engine.material;

import org.example.engine.math.Matrix4;

public class ShadowMaterial extends Material {
    private static final int MAX_BONES = 100;

    public ShadowMaterial(String frag) {
        super(frag);
    }

    public ShadowMaterial(String frag, String vert) {
        super(frag, vert);
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null) {
            return;
        }

        Matrix4 shadowMatrix = data.lightSpaceMatrix == null
                ? Matrix4.Identity()
                : data.lightSpaceMatrix;

        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        setMatrix4ToUniform("shadowMatrix", shadowMatrix);
        setVector3ToUniform("lightPos", data.lightPosition);
        setFloatToUniform("lightFar", data.lightFar);
        applySkinningUniforms(data.boneMatrices);
    }

    @Override
    public void cleanup() {

    }

    protected void applySkinningUniforms(Matrix4[] boneMatrices) {
        boolean useSkinning = boneMatrices != null && boneMatrices.length > 0;
        setIntToUniform("useSkinning", useSkinning ? 1 : 0);
        if (useSkinning) {
            setMatrix4ArrayToUniform("boneMatrices[0]", boneMatrices, MAX_BONES);
        }
    }
}
