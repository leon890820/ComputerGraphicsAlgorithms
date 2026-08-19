package org.example.engine.importer;

import org.example.engine.asset.Asset;
import org.example.engine.mesh.Mesh;

public class DefaultMeshAssetLoader implements MeshAssetLoader {

    private final AssimpAssetLoader assimpAssetLoader;
    private final AssetMeshAdapter assetMeshAdapter;

    public DefaultMeshAssetLoader() {
        this(new AssimpAssetLoader(), new AssetMeshAdapter());
    }

    public DefaultMeshAssetLoader(
            AssimpAssetLoader assimpAssetLoader,
            AssetMeshAdapter assetMeshAdapter
    ) {
        this.assimpAssetLoader = assimpAssetLoader;
        this.assetMeshAdapter = assetMeshAdapter;
    }

    @Override
    public LoadedMeshAsset load(String path) {
        if (isObjPath(path)) {
            Mesh mesh = new ObjLoader().load(stripObjExtension(path));
            return new LoadedMeshAsset(mesh, null);
        }

        Asset asset = assimpAssetLoader.load(path);
        Mesh mesh = assetMeshAdapter.toMesh(asset);
        return new LoadedMeshAsset(mesh, asset);
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
