package org.example.engine.prt;

import org.example.engine.importer.ObjLoader;
import org.example.engine.mesh.Mesh;

public class TransferBakeTool {

    public static void main(String[] args) {
        String meshPath = args.length > 0 ? args[0] : "/meshes/Furina/Furina";
        int bands = args.length > 1 ? Integer.parseInt(args[1]) : SHCoefficients.DEFAULT_BANDS;
        int sampleCount = args.length > 2 ? Integer.parseInt(args[2]) : TransferBaker.DEFAULT_SAMPLE_COUNT;

        Mesh mesh = new ObjLoader(false).load(meshPath);

        TransferBaker baker = new TransferBaker();
        baker.loadOrBake(meshPath, mesh, bands, sampleCount);

        TransferCache cache = new TransferCache();
        System.out.println("Transfer ready");
        System.out.println("source = " + meshPath);
        System.out.println("bands = " + bands);
        System.out.println("sampleCount = " + sampleCount);
        System.out.println("cache = " + cache.cachePath(meshPath, bands, sampleCount));
    }
}
