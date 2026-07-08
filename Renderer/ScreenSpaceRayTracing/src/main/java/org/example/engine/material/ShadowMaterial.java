package org.example.engine.material;

public class ShadowMaterial extends Material {

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

        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        if (data.shadowMatrix != null) {
            setMatrix4ToUniform("shadowMatrix", data.shadowMatrix);
        }
        applySkinning(data);
        setVector3ToUniform("lightPos", data.lightPosition);
        setFloatToUniform("lightFar", data.lightFar);
    }

    @Override
    public void cleanup() {

    }
}
