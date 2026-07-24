package org.example.engine.component.fluid;

import org.example.engine.render.RenderContext;

public abstract class FluidDisplay {
    private final FluidRenderMode mode;

    protected FluidDisplay(FluidRenderMode mode) {
        this.mode = mode;
    }

    public FluidRenderMode getMode() {
        return mode;
    }

    public void onAttach() {
    }

    public abstract void render(RenderContext ctx);

    public void dispose() {
    }
}
