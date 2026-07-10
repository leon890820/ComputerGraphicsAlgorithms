package org.example.engine.prt;

public class SkyboxSHBakeTool {

    public static void main(String[] args) {
        String skyboxPath = args.length > 0 ? args[0] : "/textures/Skybox/church";
        int bands = args.length > 1 ? Integer.parseInt(args[1]) : SHCoefficients.DEFAULT_BANDS;

        SkyboxSHProjector projector = new SkyboxSHProjector();
        SHCoefficients coefficients = projector.loadOrProject(skyboxPath, bands);
        SkyboxSHCache cache = new SkyboxSHCache();

        System.out.println("Skybox SH ready");
        System.out.println("source = " + skyboxPath);
        System.out.println("bands = " + coefficients.getBands());
        System.out.println("coefficientCount = " + coefficients.getCoefficientCount());
        System.out.println("cache = " + cache.cachePath(skyboxPath, bands));

        for (int i = 0; i < coefficients.getCoefficientCount(); i++) {
            System.out.println(i + ": "
                    + coefficients.r(i) + ", "
                    + coefficients.g(i) + ", "
                    + coefficients.b(i));
        }
    }
}
