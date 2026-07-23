package org.example.engine.component;

public enum FluidRenderMode {
    PARTICLES,
    MARCHING_CUBES,
    RAY_TRACING;

    public FluidRenderMode next() {
        FluidRenderMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }
}
