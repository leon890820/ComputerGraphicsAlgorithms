package org.example.engine.debug;

import imgui.type.ImBoolean;

public class MaterialDebugSettings {
    public static final int DITHER_MODE_NORMAL = 0;
    public static final int DITHER_MODE_BW = 1;
    public static final int DITHER_MODE_RGB = 2;

    public final ImBoolean showPanel = new ImBoolean(true);
    public final ImBoolean enableDither = new ImBoolean(true);
    public final int[] ditherMode = { DITHER_MODE_BW };
    public final ImBoolean enableCompareWipe = new ImBoolean(false);
    public final float[] compareWipePosition = { 0.0f };
    public final float[] compareWipeEdge = { 0.01f };
    public final float[] compareWipePeriod = { 2.0f };
    public final int[] compareWipeDirection = { 1 };

    public final float[] ditherScale = { 5.0f };
    public final float[] ditherSizeVariability = { 0.0f };
    public final float[] ditherContrast = { 1.0f };
    public final float[] ditherStretchSmoothness = { 1.0f };
    public final float[] ditherInputExposure = { 1.0f };
    public final float[] ditherInputOffset = { 0.0f };
    public final float[] ditherStrength = { 1.0f };
    public final float[] ditherPaperColor = { 0.914f, 0.894f, 0.839f };
    public final float[] ditherInkColor = { 0.078f, 0.075f, 0.102f };
    public final float[] ditherAntiAlias = { 0.75f };
    public final float[] ditherMoireFadeStart = { 18.0f };
    public final float[] ditherMoireFadeEnd = { 40.0f };

    public final float[] lightPosition = { 0.0f, -250.0f, 550.0f };
    public final float[] lightColor = { 1.4f, 1.4f, 1.4f };
    public final float[] lightRadius = { 1800.0f };
    public final float[] lightFar = { 3000.0f };

    public final float[] cameraMoveSpeed = { 3.0f };
    public final float[] cameraLookSpeed = { 0.005f };

    public float effectiveDitherStrength() {
        return enableDither.get() ? ditherStrength[0] : 0.0f;
    }
}