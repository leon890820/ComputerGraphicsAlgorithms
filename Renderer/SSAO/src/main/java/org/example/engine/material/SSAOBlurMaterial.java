package org.example.engine.material;

import org.example.engine.gl.Texture;

public class SSAOBlurMaterial extends Material {

    private Texture ssaoInput;
    private float texelSizeX = 1.0f;
    private float texelSizeY = 1.0f;

    public SSAOBlurMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public SSAOBlurMaterial setInput(Texture texture) {
        ssaoInput = texture;
        return this;
    }

    public SSAOBlurMaterial setSize(int width, int height) {
        texelSizeX = width <= 0 ? 1.0f : 1.0f / width;
        texelSizeY = height <= 0 ? 1.0f : 1.0f / height;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        setTexture("ssaoInput", ssaoInput, 0);
        setVector2ToUniform("texelSize", texelSizeX, texelSizeY);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
