package org.example.engine.gameobject;

import org.example.engine.importer.MeshAssetLoader;
import org.example.engine.material.Material;
import org.example.engine.material.PRTMaterial;
import org.example.engine.mesh.Mesh;
import org.example.engine.prt.SHCoefficients;
import org.example.engine.prt.TransferBaker;

public class PRTObject extends MeshObject {

    private String meshPath;
    private int bands = SHCoefficients.DEFAULT_BANDS;
    private int sampleCount = TransferBaker.DEFAULT_SAMPLE_COUNT;

    public PRTObject(String path) {
        load(path, new PRTMaterial());
    }

    public PRTObject(String path, PRTMaterial material) {
        load(path, material);
    }

    public PRTObject(String path, PRTMaterial material, int bands, int sampleCount) {
        this.bands = bands;
        this.sampleCount = sampleCount;
        load(path, material);
    }

    public PRTObject(MeshAssetLoader loader) {
        super(loader);
    }

    public PRTObject load(String path, PRTMaterial material) {
        meshPath = path;
        super.load(path, material);
        return this;
    }

    @Override
    public MeshObject load(String path, Material mat) {
        if (!(mat instanceof PRTMaterial)) {
            throw new IllegalArgumentException("[PRTObject] material must be PRTMaterial.");
        }
        return load(path, (PRTMaterial) mat);
    }

    public String getMeshPath() {
        return meshPath;
    }

    @Override
    protected void afterMeshLoaded(String path, Mesh mesh) {
        meshPath = path;
        new TransferBaker().loadOrBake(path, mesh, bands, sampleCount);
    }
}
