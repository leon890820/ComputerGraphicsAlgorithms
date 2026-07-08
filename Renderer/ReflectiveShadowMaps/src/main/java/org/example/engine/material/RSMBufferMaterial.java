package org.example.engine.material;

public class RSMBufferMaterial extends Material {
    public RSMBufferMaterial(String frag) {
        super(frag);
    }

    public RSMBufferMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null) {
            return;
        }

        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        if (data.lightSpaceMatrix != null) {
            setMatrix4ToUniform("lightVPMatrix", data.lightSpaceMatrix);
        }
        applySkinning(data);

        setVector3ToUniform("lightPos", data.lightPosition);
        setFloatToUniform("lightFar", data.lightFar);

        if (data.baseColorTexture != null && data.baseColorTexture.isUploaded()) {
            setTexture("tex", data.baseColorTexture, 0);
        }
    }

    @Override
    public void cleanup() {
    }
}
