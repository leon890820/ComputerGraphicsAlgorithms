package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.asset.Asset;
import org.example.engine.component.Animator;
import org.example.engine.importer.DefaultMeshAssetLoader;
import org.example.engine.importer.LoadedMeshAsset;
import org.example.engine.importer.MeshAssetLoader;

public class MeshObject extends GameObject {

    private static MeshAssetLoader defaultLoader = new DefaultMeshAssetLoader();

    public Asset asset;
    private MeshAssetLoader loader;

    public MeshObject() {
    }

    public MeshObject(String path, Material mat) {
        load(path, mat);
    }

    public MeshObject(MeshAssetLoader loader) {
        this.loader = loader;
    }

    public MeshObject(String path, Material mat, MeshAssetLoader loader) {
        this.loader = loader;
        load(path, mat);
    }

    public static void setDefaultLoader(MeshAssetLoader loader) {
        if (loader != null) {
            defaultLoader = loader;
        }
    }

    public MeshObject load(String path, Material mat) {
        LoadedMeshAsset loaded = getLoader().load(path);
        asset = loaded.getAsset();
        setAnimator(loaded.hasAnimations()
                ? new Animator(asset)
                : null);

        setMesh(loaded.getMesh());
        buildSubMeshRenderers(mat);
        return this;
    }

    public Asset getAsset() {
        return asset;
    }

    public boolean hasAnimation() {
        return hasAnimator();
    }

    private MeshAssetLoader getLoader() {
        if (loader != null) {
            return loader;
        }
        return defaultLoader;
    }
}
