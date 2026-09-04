package org.example.engine.debug;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.example.engine.asset.animation.AnimationClip;
import org.example.engine.component.core.Animator;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.render.RenderContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DebugOverlay {
    private static final Path TITLE_FONT = Path.of("C:/Windows/Fonts/OLDENGL.TTF");
    private static final Path BODY_FONT = Path.of("C:/Windows/Fonts/georgia.ttf");

    private final MaterialDebugSettings settings;
    private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gl3 = new ImGuiImplGl3();
    private final float[] scalarScratch = new float[1];
    private final float[] colorPreviewScratch = new float[4];

    private ImFont titleFont;
    private ImFont bodyFont;
    private boolean initialized;
    private boolean wantCaptureMouse;
    private boolean wantCaptureKeyboard;
    private double compareWipeStartTime;

    public DebugOverlay(MaterialDebugSettings settings) {
        this.settings = settings;
    }

    public void init(long windowHandle) {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        loadFonts(io);

        applyPaperStyle();

        glfw.init(windowHandle, true);
        gl3.init("#version 330 core");
        initialized = true;
    }

    public void render(RenderContext ctx) {
        if (!initialized) {
            return;
        }

        syncFromScene(ctx);
        updateCompareWipe(ctx);

        glfw.newFrame();
        ImGui.newFrame();

        if (settings.showPanel.get()) {
            drawPanel(ctx);
        }

        wantCaptureMouse = ImGui.getIO().getWantCaptureMouse();
        wantCaptureKeyboard = ImGui.getIO().getWantCaptureKeyboard();

        ImGui.render();
        gl3.renderDrawData(ImGui.getDrawData());
    }

    public boolean wantsCaptureMouse() {
        return wantCaptureMouse;
    }

    public boolean wantsCaptureKeyboard() {
        return wantCaptureKeyboard;
    }

    public void dispose() {
        if (!initialized) {
            return;
        }

        gl3.dispose();
        glfw.dispose();
        ImGui.destroyContext();
        initialized = false;
    }

    private void drawPanel(RenderContext ctx) {
        ImGui.setNextWindowPos(8.0f, 8.0f, ImGuiCond.Once);
        ImGui.setNextWindowSize(330.0f, 0.0f, ImGuiCond.Once);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.begin("Material Debug", settings.showPanel,
                ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.AlwaysAutoResize);

        pushTitleFont();
        ImGui.text("Material Debug");
        popTitleFont();

        drawDitherControls(ctx);
        drawCameraControls(ctx);
        drawLightControls(ctx);
        drawSceneStats(ctx);

        ImGui.end();
        ImGui.popStyleVar(2);
    }

    private void drawDitherControls(RenderContext ctx) {
        section("Dither");
        ImGui.checkbox("Enable", settings.enableDither);
        modeSelector();
        compareWipeControls(ctx);
        slider("Scale", settings.ditherScale, 2.0f, 10.0f);
        slider("Size Variability", settings.ditherSizeVariability, 0.0f, 1.0f);
        slider("Contrast", settings.ditherContrast, 0.0f, 2.0f);
        slider("Stretch Smoothness", settings.ditherStretchSmoothness, 0.0f, 2.0f);
        slider("Exposure", settings.ditherInputExposure, 0.0f, 5.0f);
        slider("Offset", settings.ditherInputOffset, -1.0f, 1.0f);
        slider("Strength", settings.ditherStrength, 0.0f, 1.0f);
        colorSliders("Paper", settings.ditherPaperColor);
        colorSliders("Ink", settings.ditherInkColor);
        slider("Anti Alias", settings.ditherAntiAlias, 0.0f, 1.0f);
        slider("Moire Fade Start", settings.ditherMoireFadeStart, 1.0f, 80.0f);
        slider("Moire Fade End", settings.ditherMoireFadeEnd, 2.0f, 120.0f);
    }

    private void drawCameraControls(RenderContext ctx) {
        section("Camera");
        slider("Move Speed", settings.cameraMoveSpeed, 0.1f, 20.0f);
        slider("Look Speed", settings.cameraLookSpeed, 0.001f, 0.03f);

        if (ctx != null && ctx.camera != null) {
            ImGui.text(String.format("Position: %.1f, %.1f, %.1f",
                    ctx.camera.transform.position.x,
                    ctx.camera.transform.position.y,
                    ctx.camera.transform.position.z));
            ImGui.text(String.format("Euler: %.3f, %.3f, %.3f",
                    ctx.camera.transform.eular.x,
                    ctx.camera.transform.eular.y,
                    ctx.camera.transform.eular.z));
        }
    }

    private void drawLightControls(RenderContext ctx) {
        section("Light");
        sliderAt("Light X", settings.lightPosition, 0, -1200.0f, 1200.0f);
        sliderAt("Light Y", settings.lightPosition, 1, -1200.0f, 800.0f);
        sliderAt("Light Z", settings.lightPosition, 2, -200.0f, 1600.0f);
        sliderAt("Light R", settings.lightColor, 0, 0.0f, 4.0f);
        sliderAt("Light G", settings.lightColor, 1, 0.0f, 4.0f);
        sliderAt("Light B", settings.lightColor, 2, 0.0f, 4.0f);
        slider("Radius", settings.lightRadius, 100.0f, 5000.0f);
        slider("Far", settings.lightFar, 100.0f, 8000.0f);

        applyToSceneLight(ctx);
    }

    private void drawSceneStats(RenderContext ctx) {
        section("Stats");
        ImGui.text(String.format("Frame Time: %.2f ms", ImGui.getIO().getDeltaTime() * 1000.0f));
        ImGui.text(String.format("Time: %.1f s", glfwGetTime()));
        if (ctx != null && ctx.scene != null) {
            ImGui.text("Objects: " + ctx.scene.getObjects().size());
            ImGui.text("Lights: " + ctx.scene.getLights().size());
        }
    }

    private void syncFromScene(RenderContext ctx) {
        PointLight light = findPointLight(ctx);
        if (light == null) {
            return;
        }

        settings.lightPosition[0] = light.transform.position.x;
        settings.lightPosition[1] = light.transform.position.y;
        settings.lightPosition[2] = light.transform.position.z;
        settings.lightColor[0] = light.light_color.x;
        settings.lightColor[1] = light.light_color.y;
        settings.lightColor[2] = light.light_color.z;
        settings.lightRadius[0] = light.getRadius();
        settings.lightFar[0] = light.getLightFar();
    }

    private void applyToSceneLight(RenderContext ctx) {
        PointLight light = findPointLight(ctx);
        if (light == null) {
            return;
        }

        light.transform.setPosition(
                settings.lightPosition[0],
                settings.lightPosition[1],
                settings.lightPosition[2]
        );
        light.light_color.set(
                settings.lightColor[0],
                settings.lightColor[1],
                settings.lightColor[2]
        );
        light.setRadius(settings.lightRadius[0]);
        light.setNearFar(1.0f, settings.lightFar[0]);
    }

    private PointLight findPointLight(RenderContext ctx) {
        if (ctx == null || ctx.scene == null) {
            return null;
        }

        for (Light light : ctx.scene.getLights()) {
            if (light instanceof PointLight) {
                return (PointLight) light;
            }
        }
        return null;
    }

    private void section(String label) {
        ImGui.spacing();
        ImGui.separator();
        pushTitleFont();
        ImGui.text(label);
        popTitleFont();
    }

    private void compareWipeControls(RenderContext ctx) {
        String label = settings.enableCompareWipe.get() ? "Stop Compare Wipe" : "Run Compare Wipe";
        if (ImGui.button(label, 150.0f, 0.0f)) {
            boolean next = !settings.enableCompareWipe.get();
            settings.enableCompareWipe.set(next);
            if (next) {
                compareWipeStartTime = glfwGetTime();
                settings.enableDither.set(true);
                settings.ditherMode[0] = MaterialDebugSettings.DITHER_MODE_BW;
            }
        }
        ImGui.sameLine();
        ImGui.text(String.format("%.2f", settings.compareWipePosition[0]));
        slider("Wipe Edge", settings.compareWipeEdge, 0.001f, 0.05f);
        settings.compareWipePeriod[0] = findAnimationPeriod(ctx);
    }

    private void updateCompareWipe(RenderContext ctx) {
        settings.compareWipePeriod[0] = findAnimationPeriod(ctx);
        if (!settings.enableCompareWipe.get()) {
            return;
        }

        float period = Math.max(0.25f, settings.compareWipePeriod[0]);
        float t = (float) ((glfwGetTime() - compareWipeStartTime) % period) / period;

        if (t < 0.4f) {
            settings.compareWipeDirection[0] = 1;
            settings.compareWipePosition[0] = smooth01(t / 0.4f);
        } else if (t < 0.6f) {
            settings.compareWipeDirection[0] = 1;
            settings.compareWipePosition[0] = 1.0f;
        } else {
            settings.compareWipeDirection[0] = -1;
            settings.compareWipePosition[0] = 1.0f - smooth01((t - 0.6f) / 0.4f);
        }
    }

    private float smooth01(float x) {
        float t = Math.max(0.0f, Math.min(1.0f, x));
        return t * t * (3.0f - 2.0f * t);
    }

    private float findAnimationPeriod(RenderContext ctx) {
        if (ctx == null || ctx.scene == null) {
            return settings.compareWipePeriod[0];
        }

        for (Animator animator : ctx.scene.getComponents(Animator.class)) {
            AnimationClip clip = animator.getCurrentClip();
            if (clip != null && clip.duration > 0.0) {
                double ticksPerSecond = clip.ticksPerSecond == 0.0 ? 25.0 : clip.ticksPerSecond;
                return (float) Math.max(0.25, clip.duration / ticksPerSecond);
            }
        }

        return settings.compareWipePeriod[0];
    }

    private void modeSelector() {
        ImGui.text("Mode");
        ImGui.sameLine(58.0f);
        if (ImGui.radioButton("Normal", settings.ditherMode[0] == MaterialDebugSettings.DITHER_MODE_NORMAL)) {
            settings.ditherMode[0] = MaterialDebugSettings.DITHER_MODE_NORMAL;
        }
        ImGui.sameLine();
        if (ImGui.radioButton("BW", settings.ditherMode[0] == MaterialDebugSettings.DITHER_MODE_BW)) {
            settings.ditherMode[0] = MaterialDebugSettings.DITHER_MODE_BW;
        }
        ImGui.sameLine();
        if (ImGui.radioButton("RGB", settings.ditherMode[0] == MaterialDebugSettings.DITHER_MODE_RGB)) {
            settings.ditherMode[0] = MaterialDebugSettings.DITHER_MODE_RGB;
        }
    }

    private void colorSliders(String label, float[] values) {
        ImGui.text(label);
        ImGui.sameLine(58.0f);
        colorComponent(label, "R", values, 0, 0.95f, 0.08f, 0.06f);
        ImGui.sameLine();
        colorComponent(label, "G", values, 1, 0.10f, 0.90f, 0.12f);
        ImGui.sameLine();
        colorComponent(label, "B", values, 2, 0.16f, 0.28f, 1.00f);
        ImGui.sameLine();
        colorPreview(label, values);
    }

    private void colorComponent(String group, String channel, float[] values, int index, float r, float g, float b) {
        ImGui.textColored(r, g, b, 1.0f, channel);
        ImGui.sameLine();
        scalarScratch[0] = values[index];
        ImGui.pushStyleColor(ImGuiCol.FrameBg, r, g, b, 0.20f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, r, g, b, 0.32f);
        ImGui.pushStyleColor(ImGuiCol.FrameBgActive, r, g, b, 0.45f);
        ImGui.pushStyleColor(ImGuiCol.SliderGrab, r, g, b, 0.95f);
        ImGui.setNextItemWidth(42.0f);
        ImGui.sliderFloat("##" + group + channel, scalarScratch, 0.0f, 1.0f, "%.3f");
        ImGui.popStyleColor(4);
        values[index] = scalarScratch[0];
    }

    private void colorPreview(String label, float[] values) {
        colorPreviewScratch[0] = values[0];
        colorPreviewScratch[1] = values[1];
        colorPreviewScratch[2] = values[2];
        colorPreviewScratch[3] = 1.0f;
        ImGui.colorButton("##" + label + "Preview", colorPreviewScratch, 0, 24.0f, 16.0f);
    }

    private void slider(String label, float[] value, float min, float max) {
        ImGui.pushItemWidth(190.0f);
        ImGui.sliderFloat(label, value, min, max, "%.3f");
        ImGui.popItemWidth();
    }

    private void sliderAt(String label, float[] values, int index, float min, float max) {
        scalarScratch[0] = values[index];
        slider(label, scalarScratch, min, max);
        values[index] = scalarScratch[0];
    }

    private void loadFonts(ImGuiIO io) {
        if (Files.exists(BODY_FONT)) {
            bodyFont = io.getFonts().addFontFromFileTTF(BODY_FONT.toString(), 15.0f);
            io.setFontDefault(bodyFont);
        }

        if (Files.exists(TITLE_FONT)) {
            titleFont = io.getFonts().addFontFromFileTTF(TITLE_FONT.toString(), 18.0f);
        }
    }

    private void pushTitleFont() {
        if (titleFont != null) {
            ImGui.pushFont(titleFont);
        }
    }

    private void popTitleFont() {
        if (titleFont != null) {
            ImGui.popFont();
        }
    }

    private void applyPaperStyle() {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowPadding(8.0f, 7.0f);
        style.setFramePadding(4.0f, 2.0f);
        style.setItemSpacing(6.0f, 4.0f);
        style.setWindowRounding(0.0f);
        style.setFrameRounding(0.0f);
        style.setGrabRounding(0.0f);
        style.setWindowBorderSize(1.0f);
        style.setAlpha(0.88f);

        style.setColor(ImGuiCol.WindowBg, 0.82f, 0.80f, 0.70f, 0.72f);
        style.setColor(ImGuiCol.Border, 0.18f, 0.17f, 0.14f, 0.75f);
        style.setColor(ImGuiCol.Text, 0.05f, 0.045f, 0.035f, 1.0f);
        style.setColor(ImGuiCol.TitleBg, 0.56f, 0.54f, 0.47f, 0.85f);
        style.setColor(ImGuiCol.TitleBgActive, 0.50f, 0.49f, 0.42f, 0.95f);
        style.setColor(ImGuiCol.FrameBg, 0.70f, 0.69f, 0.61f, 0.62f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.78f, 0.76f, 0.66f, 0.80f);
        style.setColor(ImGuiCol.FrameBgActive, 0.66f, 0.64f, 0.55f, 0.95f);
        style.setColor(ImGuiCol.SliderGrab, 0.12f, 0.12f, 0.13f, 0.95f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.02f, 0.02f, 0.02f, 1.0f);
        style.setColor(ImGuiCol.CheckMark, 0.05f, 0.05f, 0.05f, 1.0f);
        style.setColor(ImGuiCol.Separator, 0.24f, 0.23f, 0.20f, 0.45f);
    }
}