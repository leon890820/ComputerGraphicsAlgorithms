package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.gl.Texture3D;
import org.example.engine.gl.TextureCube;

public class PointLightMaterial extends LightMaterial{
    TextureCube shadowCubeMap;
    private Texture3D ditherTexture;
    private Texture ditherRampTexture;
    private float ditherScale = 5.0f;
    private float ditherSizeVariability = 0.0f;
    private float ditherContrast = 1.0f;
    private float ditherStretchSmoothness = 1.0f;
    private float ditherInputExposure = 1.0f;
    private float ditherInputOffset = 0.0f;
    private float ditherStrength = 1.0f;
    private int ditherMode = 1;
    private boolean compareWipeEnabled = false;
    private float compareWipePosition = 0.0f;
    private float compareWipeEdge = 0.01f;
    private int compareWipeDirection = 1;
    private final float[] ditherPaperColor = { 0.914f, 0.894f, 0.839f };
    private final float[] ditherInkColor = { 0.078f, 0.075f, 0.102f };
    private float ditherAntiAlias = 0.75f;
    private float ditherMoireFadeStart = 18.0f;
    private float ditherMoireFadeEnd = 40.0f;

    public PointLightMaterial(String frag) {
        super(frag);
    }

    public PointLightMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public LightMaterial setDepthTex(TextureCube t) {
        shadowCubeMap = t;
        return this;
    }

    public PointLightMaterial setDitherTexture(Texture3D ditherTexture) {
        this.ditherTexture = ditherTexture;
        return this;
    }

    public PointLightMaterial setDitherRampTexture(Texture ditherRampTexture) {
        this.ditherRampTexture = ditherRampTexture;
        return this;
    }

    public PointLightMaterial setDitherScale(float ditherScale) {
        this.ditherScale = ditherScale;
        return this;
    }

    public PointLightMaterial setDitherSizeVariability(float ditherSizeVariability) {
        this.ditherSizeVariability = ditherSizeVariability;
        return this;
    }

    public PointLightMaterial setDitherContrast(float ditherContrast) {
        this.ditherContrast = ditherContrast;
        return this;
    }

    public PointLightMaterial setDitherStretchSmoothness(float ditherStretchSmoothness) {
        this.ditherStretchSmoothness = ditherStretchSmoothness;
        return this;
    }

    public PointLightMaterial setDitherInputExposure(float ditherInputExposure) {
        this.ditherInputExposure = ditherInputExposure;
        return this;
    }

    public PointLightMaterial setDitherInputOffset(float ditherInputOffset) {
        this.ditherInputOffset = ditherInputOffset;
        return this;
    }

    public PointLightMaterial setDitherStrength(float ditherStrength) {
        this.ditherStrength = ditherStrength;
        return this;
    }

    public PointLightMaterial setDitherMode(int ditherMode) {
        this.ditherMode = ditherMode;
        return this;
    }

    public PointLightMaterial setCompareWipe(boolean enabled, float position, float edge, int direction) {
        this.compareWipeEnabled = enabled;
        this.compareWipePosition = position;
        this.compareWipeEdge = edge;
        this.compareWipeDirection = direction;
        return this;
    }

    public PointLightMaterial setDitherPaperColor(float r, float g, float b) {
        ditherPaperColor[0] = r;
        ditherPaperColor[1] = g;
        ditherPaperColor[2] = b;
        return this;
    }

    public PointLightMaterial setDitherInkColor(float r, float g, float b) {
        ditherInkColor[0] = r;
        ditherInkColor[1] = g;
        ditherInkColor[2] = b;
        return this;
    }

    public PointLightMaterial setDitherAntiAlias(float ditherAntiAlias) {
        this.ditherAntiAlias = ditherAntiAlias;
        return this;
    }

    public PointLightMaterial setDitherMoireFadeRange(float start, float end) {
        this.ditherMoireFadeStart = start;
        this.ditherMoireFadeEnd = end;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        setTexture("albedo", albedoTex, 0);
        setTexture("worldNormal", normalTex, 1);
        setTexture("worldPos", positionTex, 2);
        setCubeTexture("shadowCubeMap", shadowCubeMap, 3);

        if (ditherTexture != null && ditherTexture.isUploaded()) {
            ditherTexture.bind(4);
            setIntToUniform("ditherTex", 4);
            setVector3ToUniform(
                    "ditherTexSize",
                    ditherTexture.getWidth(),
                    ditherTexture.getHeight(),
                    ditherTexture.getDepth()
            );
            setIntToUniform("useDitherTex", 1);
        } else {
            setIntToUniform("useDitherTex", 0);
            setVector3ToUniform("ditherTexSize", 1.0f, 1.0f, 1.0f);
        }

        if (ditherRampTexture != null && ditherRampTexture.isUploaded()) {
            setTexture("ditherRampTex", ditherRampTexture, 5);
            setIntToUniform("useDitherRampTex", 1);
        } else {
            setIntToUniform("useDitherRampTex", 0);
        }

        setFloatToUniform("ditherScale", ditherScale);
        setFloatToUniform("ditherSizeVariability", ditherSizeVariability);
        setFloatToUniform("ditherContrast", ditherContrast);
        setFloatToUniform("ditherStretchSmoothness", ditherStretchSmoothness);
        setFloatToUniform("ditherInputExposure", ditherInputExposure);
        setFloatToUniform("ditherInputOffset", ditherInputOffset);
        setFloatToUniform("ditherStrength", ditherStrength);
        setIntToUniform("ditherMode", ditherMode);
        setIntToUniform("compareWipeEnabled", compareWipeEnabled ? 1 : 0);
        setFloatToUniform("compareWipePosition", compareWipePosition);
        setFloatToUniform("compareWipeEdge", compareWipeEdge);
        setIntToUniform("compareWipeDirection", compareWipeDirection);
        setVector3ToUniform("ditherPaperColor", ditherPaperColor[0], ditherPaperColor[1], ditherPaperColor[2]);
        setVector3ToUniform("ditherInkColor", ditherInkColor[0], ditherInkColor[1], ditherInkColor[2]);
        setFloatToUniform("ditherAntiAlias", ditherAntiAlias);
        setFloatToUniform("ditherMoireFadeStart", ditherMoireFadeStart);
        setFloatToUniform("ditherMoireFadeEnd", ditherMoireFadeEnd);
        applyLightUniforms(data);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (ditherTexture != null) {
            ditherTexture.unbind(4);
        }
        unbindTexture(5);
    }
}
