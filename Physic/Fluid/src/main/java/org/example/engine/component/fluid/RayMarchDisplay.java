package org.example.engine.component.fluid;

import org.example.engine.component.render.MeshRenderer;
import org.example.engine.material.FluidRayMarchMaterial;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33.glDepthMask;
import static org.lwjgl.opengl.GL33.glDisable;
import static org.lwjgl.opengl.GL33.glEnable;

public class RayMarchDisplay extends FluidDisplay {
    private final FluidSimulation simulation;
    private final FluidRayMarchMaterial material;

    public RayMarchDisplay(FluidSimulation simulation) {
        super(FluidRenderMode.RAY_TRACING);
        this.simulation = simulation;
        material = new FluidRayMarchMaterial();
    }

    @Override
    public void render(RenderContext ctx) {
        if (ctx == null || ctx.camera == null) {
            return;
        }

        material
                .setDensityTexture(simulation.getSimulator().getDensityVolumeTexture())
                .setCamera(ctx.camera)
                .setBounds(
                        simulation.getSimulator().getDensityBoundsCenter(),
                        simulation.getSimulator().getDensityBoundsSize()
                );
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        for (MeshRenderer renderer : ctx.camera.getMeshRenderers()) {
            renderer.render(ctx, material);
        }
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
    }

    @Override
    public void dispose() {
        material.dispose();
    }
}
