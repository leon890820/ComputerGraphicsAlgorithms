package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.prt.SHCoefficients;
import org.example.engine.prt.SkyboxSHProjector;

import java.util.Set;

public class PRTMaterial extends Material {

    public static final String DEFAULT_SKYBOX_PATH = "/textures/Skybox/church";

    private static final int SUPPORTED_BANDS = 3;
    private static final int COEFFICIENT_COUNT = SUPPORTED_BANDS * SUPPORTED_BANDS;

    private final SHCoefficients lightCoefficients;
    private Texture albedoTexture;

    public PRTMaterial() {
        this(DEFAULT_SKYBOX_PATH, SUPPORTED_BANDS);
    }

    public PRTMaterial(String skyboxPath, int bands) {
        super("/shaders/prt.frag", "/shaders/prt.vert");

        if (bands != SUPPORTED_BANDS) {
            throw new IllegalArgumentException("[PRTMaterial] first GPU path supports 3 bands only.");
        }

        lightCoefficients = new SkyboxSHProjector().loadOrProject(skyboxPath, bands);
    }

    public PRTMaterial setTexture(Texture texture) {
        albedoTexture = texture;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.mvpMatrix == null) {
            System.out.println("[PRTMaterial] render data is missing model or MVP matrix.");
            return;
        }

        setMatrix4ToUniform("MVP", data.mvpMatrix);
        setMatrix4ToUniform("modelMatrix", data.modelMatrix);

        for (int i = 0; i < COEFFICIENT_COUNT; i++) {
            setVector3ToUniform(
                    "lightSH[" + i + "]",
                    lightCoefficients.r(i),
                    lightCoefficients.g(i),
                    lightCoefficients.b(i)
            );
        }

        Texture useTex = albedoTexture != null ? albedoTexture : data.baseColorTexture;
        setIntToUniform("useTexture", useTex != null && useTex.isUploaded() ? 1 : 0);
        if (useTex != null && useTex.isUploaded()) {
            setTexture("tex", useTex, 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }

    @Override
    public void collectTextures(Set<Texture> textures) {
        if (albedoTexture != null) {
            textures.add(albedoTexture);
        }
    }
}
