package org.example.engine.material;

import org.example.engine.gl.Texture;

import java.util.Set;

public class PortalMaterial extends Material {
    private Texture renderTexture;
    private Texture fallbackTexture;

    public PortalMaterial() {
        this("/shaders/portal.frag", "/shaders/portal.vert");
    }

    public PortalMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public PortalMaterial setRenderTexture(Texture texture) {
        renderTexture = texture;
        return this;
    }

    public PortalMaterial setFallbackTexture(Texture texture) {
        fallbackTexture = texture;
        return this;
    }

    public Texture getRenderTexture() {
        return renderTexture;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.mvpMatrix == null) {
            System.out.println("[PortalMaterial] render data is missing MVP matrix.");
            return;
        }

        setMatrix4ToUniform("MVP", data.mvpMatrix);

        Texture texture = renderTexture != null && renderTexture.isUploaded()
                ? renderTexture
                : fallbackTexture;

        if (texture != null && texture.isUploaded()) {
            setTexture("tex", texture, 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }

    @Override
    public void collectTextures(Set<Texture> textures) {
        if (fallbackTexture != null) {
            textures.add(fallbackTexture);
        }
    }
}
