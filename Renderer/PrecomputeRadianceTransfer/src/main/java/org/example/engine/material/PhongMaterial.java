package org.example.engine.material;

import org.example.engine.gl.Texture;

import java.util.Set;

public class PhongMaterial extends Material {

    private static final int MAX_BONES = 100;

    Texture texture;

    public PhongMaterial(String frag) {
        super(frag);
    }

    public PhongMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public PhongMaterial setTexture(Texture t) {
        texture = t;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.mvpMatrix == null) {
            System.out.println("[PhongMaterial] render data is missing model or MVP matrix.");
            return;
        }

        setMatrix4ToUniform("MVP", data.mvpMatrix);
        setMatrix4ToUniform("modelMatrix", data.modelMatrix);

        boolean useSkinning = data.boneMatrices != null && data.boneMatrices.length > 0;
        setIntToUniform("useSkinning", useSkinning ? 1 : 0);
        if (useSkinning) {
            setMatrix4ArrayToUniform("boneMatrices[0]", data.boneMatrices, MAX_BONES);
        }

        setVector3ToUniform("ambient_light", 0.5f, 0.5f, 0.5f);

        setVector3ToUniform("view_pos", data.viewPosition);

        if (data.hasLight) {
            setVector3ToUniform("light_color", data.lightColor);
            setVector3ToUniform("light_dir", data.lightDirection);
        }

        Texture useTex = texture != null ? texture : data.baseColorTexture;

        if (useTex != null && useTex.isUploaded()) {

            setTexture("tex", useTex, 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }

    @Override
    public void collectTextures(Set<Texture> textures) {
        if (texture != null) {
            textures.add(texture);
        }
    }
}
