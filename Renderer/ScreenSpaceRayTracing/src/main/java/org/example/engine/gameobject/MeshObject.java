package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.importer.ObjLoader;
import org.example.engine.asset.Asset;
import org.example.engine.importer.AssetMeshAdapter;
import org.example.engine.importer.AssimpAssetLoader;
import org.example.engine.component.Animator;

public class MeshObject extends GameObject {

    public Asset asset;

    public MeshObject() {
    }

    public MeshObject(String path, Material mat) {
        load(path, mat);
    }

    public MeshObject load(String path, Material mat) {
        if (isObjPath(path)) {
            asset = null;
            setAnimator(null);
            setMesh(new ObjLoader().load(stripObjExtension(path)));
            buildSubMeshRenderers(mat);
            return this;
        }

        asset = new AssimpAssetLoader().load(path);
        setAnimator(asset != null && asset.hasAnimations()
                ? new Animator(asset)
                : null);

        setMesh(new AssetMeshAdapter().toMesh(asset));
        buildSubMeshRenderers(mat);
        return this;
    }

    public Asset getAsset() {
        return asset;
    }

    public boolean hasAnimation() {
        return hasAnimator();
    }

    private boolean isObjPath(String path) {
        String lower = path == null ? "" : path.toLowerCase();
        return lower.endsWith(".obj") || !fileName(lower).contains(".");
    }

    private String stripObjExtension(String path) {
        if (path == null) {
            return "";
        }

        String lower = path.toLowerCase();
        if (lower.endsWith(".obj")) {
            return path.substring(0, path.length() - 4);
        }

        return path;
    }

    private String fileName(String path) {
        if (path == null) {
            return "";
        }

        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}
