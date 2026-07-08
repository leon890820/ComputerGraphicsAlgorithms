package org.example.engine.material;

public class GBufferMaterial extends Material {
    public GBufferMaterial(String frag) {
        super(frag);
    }

    public GBufferMaterial(String frag, String vert) {
        super(frag, vert);
    }

    @Override
    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.viewMatrix == null || data.projectionMatrix == null) {
            System.out.println("[GBufferMaterial] render data is missing camera or model matrix.");
            return;
        }

        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        setMatrix4ToUniform("viewMatrix", data.viewMatrix);
        setMatrix4ToUniform("projectMatrix", data.projectionMatrix);
        applySkinning(data);

        if (data.baseColorTexture != null && data.baseColorTexture.isUploaded()) {
            setTexture("tex", data.baseColorTexture, 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
