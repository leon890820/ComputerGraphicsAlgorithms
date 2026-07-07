package org.example.engine.resource;

import org.example.engine.gl.Texture;
import org.example.engine.material.Material;

import java.util.HashSet;
import java.util.Set;

public class ResourceDisposalContext {

    private final Set<Material> materials = new HashSet<>();
    private final Set<Texture> textures = new HashSet<>();

    public void trackMaterial(Material material) {
        if (material == null) {
            return;
        }

        materials.add(material);
        material.collectTextures(textures);
    }

    public void trackTexture(Texture texture) {
        if (texture != null) {
            textures.add(texture);
        }
    }

    public void disposeAll() {
        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }

        for (Material material : materials) {
            if (material != null) {
                material.dispose();
            }
        }

        textures.clear();
        materials.clear();
    }
}
