package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.math.Vector3;

import java.util.Set;

public class PhongMaterial extends Material {

    private static final int MAX_BONES = 100;

    Texture texture;
    Vector3 baseColor = new Vector3(0.55f, 0.55f, 0.55f);

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

    public PhongMaterial setBaseColor(Vector3 color) {
        if (color != null) {
            baseColor = color;
        }
        return this;
    }

    public PhongMaterial setBaseColor(float r, float g, float b) {
        baseColor = new Vector3(r, g, b);
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

        Texture useTex = texture != null ? texture : data.baseColorTexture;
        boolean hasTexture = useTex != null && useTex.isUploaded();
        setIntToUniform("useTexture", hasTexture ? 1 : 0);
        setVector3ToUniform("baseColor", baseColor);

        if (hasTexture) {
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
