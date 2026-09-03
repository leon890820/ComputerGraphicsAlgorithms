package org.example.engine.importer;

import org.example.engine.asset.Asset;
import org.example.engine.mesh.Mesh;

public class LoadedMeshAsset {

    private final Mesh mesh;
    private final Asset asset;

    public LoadedMeshAsset(Mesh mesh, Asset asset) {
        this.mesh = mesh;
        this.asset = asset;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Asset getAsset() {
        return asset;
    }

    public boolean hasAnimations() {
        return asset != null && asset.hasAnimations();
    }
}
