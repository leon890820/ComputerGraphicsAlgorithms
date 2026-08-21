package org.example.engine.material;

public class EdgeMaterial extends Material {

    private static final int MAX_BONES = 100;

    public EdgeMaterial(String frag, String vert) {
        super(frag, vert);
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.mvpMatrix == null) {
            return;
        }

        setMatrix4ToUniform("MVP", data.mvpMatrix);

        boolean useSkinning = data.boneMatrices != null && data.boneMatrices.length > 0;
        setIntToUniform("useSkinning", useSkinning ? 1 : 0);
        if (useSkinning) {
            setMatrix4ArrayToUniform("boneMatrices[0]", data.boneMatrices, MAX_BONES);
        }
    }
}
