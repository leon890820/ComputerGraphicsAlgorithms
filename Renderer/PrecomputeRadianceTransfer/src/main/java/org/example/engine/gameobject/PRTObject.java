package org.example.engine.gameobject;

import org.example.engine.importer.MeshAssetLoader;
import org.example.engine.material.Material;
import org.example.engine.material.PRTMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.Mesh;
import org.example.engine.prt.PRTBakeMode;
import org.example.engine.prt.PRTReflectionMode;
import org.example.engine.prt.SHCoefficients;
import org.example.engine.prt.TransferBaker;

public class PRTObject extends MeshObject {

    private String meshPath;
    private int bands = SHCoefficients.DEFAULT_BANDS;
    private int sampleCount = TransferBaker.DEFAULT_SAMPLE_COUNT;
    private PRTBakeMode bakeMode = PRTBakeMode.UNSHADOW;
    private PRTReflectionMode reflectionMode = PRTReflectionMode.DIFFUSE;
    private Vector3 fixedViewPosition = TransferBaker.DEFAULT_FIXED_GLOSSY_CAMERA_POSITION;

    public PRTObject(String path) {
        load(path, new PRTMaterial());
    }

    public PRTObject(String path, PRTMaterial material) {
        load(path, material);
    }

    public PRTObject(String path, PRTMaterial material, int bands, int sampleCount) {
        this(path, material, bands, sampleCount, PRTBakeMode.UNSHADOW);
    }

    public PRTObject(String path, PRTMaterial material, int bands, int sampleCount, PRTBakeMode bakeMode) {
        this(path, material, bands, sampleCount, bakeMode, PRTReflectionMode.DIFFUSE);
    }

    public PRTObject(
            String path,
            PRTMaterial material,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode
    ) {
        this(path, material, bands, sampleCount, bakeMode, reflectionMode, TransferBaker.DEFAULT_FIXED_GLOSSY_CAMERA_POSITION);
    }

    public PRTObject(
            String path,
            PRTMaterial material,
            int bands,
            int sampleCount,
            PRTBakeMode bakeMode,
            PRTReflectionMode reflectionMode,
            Vector3 fixedViewPosition
    ) {
        this.bands = bands;
        this.sampleCount = sampleCount;
        this.bakeMode = bakeMode == null ? PRTBakeMode.UNSHADOW : bakeMode;
        this.reflectionMode = reflectionMode == null ? PRTReflectionMode.DIFFUSE : reflectionMode;
        this.fixedViewPosition = fixedViewPosition == null
                ? TransferBaker.DEFAULT_FIXED_GLOSSY_CAMERA_POSITION
                : fixedViewPosition;
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

    public PRTBakeMode getBakeMode() {
        return bakeMode;
    }

    public PRTReflectionMode getReflectionMode() {
        return reflectionMode;
    }

    public PRTObject setFixedViewPosition(Vector3 fixedViewPosition) {
        this.fixedViewPosition = fixedViewPosition == null
                ? TransferBaker.DEFAULT_FIXED_GLOSSY_CAMERA_POSITION
                : fixedViewPosition;
        return this;
    }

    @Override
    protected void afterMeshLoaded(String path, Mesh mesh) {
        meshPath = path;
        new TransferBaker()
                .setFixedGlossyCameraPosition(fixedViewPosition)
                .loadOrBake(path, mesh, bands, sampleCount, bakeMode, reflectionMode);
    }
}
