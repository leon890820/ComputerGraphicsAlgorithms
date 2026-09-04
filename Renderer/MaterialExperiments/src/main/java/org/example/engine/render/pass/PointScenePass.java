package org.example.engine.render.pass;

import org.example.engine.component.render.MeshRenderer;
import org.example.engine.debug.MaterialDebugSettings;
import org.example.engine.gl.Texture;
import org.example.engine.gl.Texture3D;
import org.example.engine.gl.Texture3DData;
import org.example.engine.gl.TextureCube;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.material.PointLightMaterial;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;
import org.example.experiments.Dither3DTextureFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_REPEAT;

public class PointScenePass extends RenderPass {
    private static final String DITHER_8X8_FILE = "dither3d_8x8.m3td";

    private final MaterialDebugSettings debugSettings;

    PointLightMaterial pointLightMaterial;
    private Texture3D ditherTexture;
    private Texture ditherRampTexture;

    public PointScenePass() {
        this(new MaterialDebugSettings());
    }

    public PointScenePass(MaterialDebugSettings debugSettings){
        this.debugSettings = debugSettings;
        pointLightMaterial = new PointLightMaterial("/shaders/materialExperimentPointLight.frag", "/shaders/quad.vert");
        createDitherTextures();
        pointLightMaterial
                .setDitherTexture(ditherTexture)
                .setDitherRampTexture(ditherRampTexture);
    }

    public void render(RenderContext ctx, GBuffer gBuffer, PointLight light, TextureCube shadowDepth) {
        applyDebugSettings();
        pointLightMaterial
                .setAlbedoTex(gBuffer.albedo)
                .setNormalTex(gBuffer.normal)
                .setPositionTex(gBuffer.position);
        pointLightMaterial.setDepthTex(shadowDepth);
        Light previousLight = ctx.activeLight;
        ctx.activeLight = light;
        try {
            for (MeshRenderer renderer : ctx.camera.getMeshRenderers()) {
                renderer.render(ctx, pointLightMaterial);
            }
        } finally {
            ctx.activeLight = previousLight;
        }
    }

    private void applyDebugSettings() {
        pointLightMaterial
                .setDitherScale(debugSettings.ditherScale[0])
                .setDitherSizeVariability(debugSettings.ditherSizeVariability[0])
                .setDitherContrast(debugSettings.ditherContrast[0])
                .setDitherStretchSmoothness(debugSettings.ditherStretchSmoothness[0])
                .setDitherInputExposure(debugSettings.ditherInputExposure[0])
                .setDitherInputOffset(debugSettings.ditherInputOffset[0])
                .setDitherStrength(debugSettings.effectiveDitherStrength())
                .setDitherMode(debugSettings.ditherMode[0])
                .setCompareWipe(
                        debugSettings.enableCompareWipe.get(),
                        debugSettings.compareWipePosition[0],
                        debugSettings.compareWipeEdge[0],
                        debugSettings.compareWipeDirection[0]
                )
                .setDitherPaperColor(
                        debugSettings.ditherPaperColor[0],
                        debugSettings.ditherPaperColor[1],
                        debugSettings.ditherPaperColor[2]
                )
                .setDitherInkColor(
                        debugSettings.ditherInkColor[0],
                        debugSettings.ditherInkColor[1],
                        debugSettings.ditherInkColor[2]
                )
                .setDitherAntiAlias(debugSettings.ditherAntiAlias[0])
                .setDitherMoireFadeRange(
                        debugSettings.ditherMoireFadeStart[0],
                        debugSettings.ditherMoireFadeEnd[0]
                );
    }

    private void createDitherTextures() {
        Path cachePath = resolveDitherCachePath();
        try {
            Texture3DData data = Dither3DTextureFactory.loadOrCreateBayer8x8(cachePath);
            ditherTexture = data.upload().setWrapMode(GL_REPEAT);
            ditherRampTexture = Dither3DTextureFactory.createRampTexture(data);
        } catch (IOException e) {
            System.out.println("[PointScenePass] Failed to load Dither3D texture: " + e.getMessage());
            ditherTexture = null;
            ditherRampTexture = null;
        }
    }

    private Path resolveDitherCachePath() {
        Path cwd = Path.of("").toAbsolutePath().normalize();

        Path projectLocal = cwd.resolve("data").resolve(DITHER_8X8_FILE).normalize();
        if (Files.exists(cwd.resolve("build.gradle")) || Files.exists(projectLocal)) {
            return projectLocal;
        }

        Path repoLocal = cwd.resolve("Renderer")
                .resolve("MaterialExperiments")
                .resolve("data")
                .resolve(DITHER_8X8_FILE)
                .normalize();
        if (Files.exists(repoLocal)) {
            return repoLocal;
        }

        Path parent = cwd;
        while (parent != null) {
            Path candidate = parent.resolve("Renderer")
                    .resolve("MaterialExperiments")
                    .resolve("data")
                    .resolve(DITHER_8X8_FILE)
                    .normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
            parent = parent.getParent();
        }

        return projectLocal;
    }
}
