package org.example.engine.component.fluid;

import org.example.engine.component.core.Component;
import org.example.engine.component.render.MeshRenderer;
import org.example.engine.material.VolumeSliceMaterial;
import org.example.engine.render.RenderContext;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL33.GL_BLEND;
import static org.lwjgl.opengl.GL33.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33.glDisable;
import static org.lwjgl.opengl.GL33.glEnable;
import static org.lwjgl.opengl.GL33.glViewport;

public class FluidRenderer extends Component {
    private static final int DENSITY_PREVIEW_SIZE = 220;
    private static final int DENSITY_PREVIEW_MARGIN = 12;
    private static final float DEFAULT_DENSITY_SLICE_DEPTH = 0.5f;

    private final ParticleSpawn spawn;
    private final FluidSimulation simulation;
    private final ArrayList<FluidDisplay> displays = new ArrayList<>();
    private final VolumeSliceMaterial densitySliceMaterial = new VolumeSliceMaterial(
            "/shaders/debug/density_slice.frag",
            "/shaders/core/quad.vert"
    );

    private SlimeAabbGizmo slimeAabbGizmo;
    private int currentDisplayIndex;
    private boolean debugDrawEnabled = true;

    public FluidRenderer(FluidSimulation simulation, ParticleSpawn spawnComponent) {
        this.simulation = simulation;
        spawn = spawnComponent == null ? new ParticleSpawn() : spawnComponent;
    }

    @Override
    protected void onAttach() {
        displays.clear();
        addDisplay(new ParticleDisplay(gameObject, simulation, spawn));
        addDisplay(new MarchingCubeDisplay(simulation));
        addDisplay(new RayMarchDisplay(simulation));
        slimeAabbGizmo = new SlimeAabbGizmo(simulation.getSimulator());
    }

    @Override
    public void render(RenderContext ctx) {
        FluidDisplay display = getCurrentDisplay();

        if (display != null) {
            applySceneTextures(ctx, display);
            display.render(ctx);
        }

        if (debugDrawEnabled) {
            simulation.getSimulator().getCollider().debugDraw(ctx);
            simulation.getSimulator().getCollider().drawGizmo(gameObject.transform, ctx);
        }

        if (debugDrawEnabled && slimeAabbGizmo != null) {
            slimeAabbGizmo.debugDraw(ctx);
        }

        if (debugDrawEnabled) {
            renderDensityPreview(ctx);
        }
    }

    public FluidRenderMode getFluidRenderMode() {
        FluidDisplay display = getCurrentDisplay();
        return display == null ? FluidRenderMode.PARTICLES : display.getMode();
    }

    public FluidRenderer setFluidRenderMode(FluidRenderMode fluidRenderMode) {
        if (fluidRenderMode == null) {
            fluidRenderMode = FluidRenderMode.PARTICLES;
        }

        for (int i = 0; i < displays.size(); i++) {
            if (displays.get(i).getMode() == fluidRenderMode) {
                currentDisplayIndex = i;
                return this;
            }
        }

        return this;
    }

    public FluidRenderMode toggleFluidRenderMode() {
        if (!displays.isEmpty()) {
            currentDisplayIndex = (currentDisplayIndex + 1) % displays.size();
        }

        return getFluidRenderMode();
    }

    public FluidRenderer setDebugDrawEnabled(boolean debugDrawEnabled) {
        this.debugDrawEnabled = debugDrawEnabled;
        simulation.getSimulator().getCollider().setDebugDrawEnabled(debugDrawEnabled);
        if (slimeAabbGizmo != null) {
            slimeAabbGizmo.setDebugDrawEnabled(debugDrawEnabled);
        }
        return this;
    }

    public boolean isDebugDrawEnabled() {
        return debugDrawEnabled;
    }

    public boolean toggleDebugDrawEnabled() {
        setDebugDrawEnabled(!debugDrawEnabled);
        return debugDrawEnabled;
    }

    @Override
    public void dispose() {
        for (FluidDisplay display : displays) {
            display.dispose();
        }
        displays.clear();
        if (slimeAabbGizmo != null) {
            slimeAabbGizmo.dispose();
            slimeAabbGizmo = null;
        }
        densitySliceMaterial.dispose();
    }

    private void addDisplay(FluidDisplay display) {
        if (display == null) {
            return;
        }

        displays.add(display);
        display.onAttach();
    }

    private FluidDisplay getCurrentDisplay() {
        if (displays.isEmpty()) {
            return null;
        }

        currentDisplayIndex = Math.max(0, Math.min(currentDisplayIndex, displays.size() - 1));
        return displays.get(currentDisplayIndex);
    }

    private void applySceneTextures(RenderContext ctx, FluidDisplay display) {
        if (ctx == null || display == null || display.getMode() != FluidRenderMode.MARCHING_CUBES) {
            return;
        }

        if (display instanceof MarchingCubeDisplay) {
            MarchingCubeDisplay marchingCubeDisplay = (MarchingCubeDisplay) display;
            marchingCubeDisplay.setSceneTextures(
                    ctx.sceneColorTexture,
                    ctx.sceneDepthTexture,
                    ctx.screenWidth,
                    ctx.screenHeight
            );
        }
    }

    private void renderDensityPreview(RenderContext ctx) {
        if (ctx == null || ctx.camera == null) {
            return;
        }

        int size = Math.min(DENSITY_PREVIEW_SIZE, Math.max(64, Math.min(ctx.screenWidth, ctx.screenHeight) / 3));
        int x = DENSITY_PREVIEW_MARGIN;
        int y = Math.max(0, ctx.screenHeight - size - DENSITY_PREVIEW_MARGIN);

        glViewport(x, y, size, size);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        densitySliceMaterial
                .setVolumeTexture(simulation.getSimulator().getDensityVolumeTexture())
                .setSliceDepth(DEFAULT_DENSITY_SLICE_DEPTH);
        for (MeshRenderer renderer : ctx.camera.getMeshRenderers()) {
            renderer.render(ctx, densitySliceMaterial);
        }

        glViewport(0, 0, ctx.screenWidth, ctx.screenHeight);
        glEnable(GL_DEPTH_TEST);
    }
}
