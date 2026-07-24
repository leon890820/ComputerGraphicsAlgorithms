package org.example.engine.component.fluid;

import org.example.engine.gl.Texture;
import org.example.engine.render.RenderContext;

public class MarchingCubeDisplay extends FluidDisplay {
    private final FluidSimulation simulation;
    private final MarchingCubeFluidRenderer renderer;

    public MarchingCubeDisplay(FluidSimulation simulation) {
        super(FluidRenderMode.MARCHING_CUBES);
        this.simulation = simulation;
        renderer = new MarchingCubeFluidRenderer();
    }

    @Override
    public void render(RenderContext ctx) {
        renderer.render(ctx, simulation.getSimulator());
    }

    public MarchingCubeDisplay setSceneTextures(
            Texture sceneColorTexture,
            Texture sceneDepthTexture,
            int screenWidth,
            int screenHeight
    ) {
        renderer.setSceneTextures(sceneColorTexture, sceneDepthTexture, screenWidth, screenHeight);
        return this;
    }

    @Override
    public void dispose() {
        renderer.dispose(null);
    }
}
