package org.example.engine.component.render;

import org.example.engine.component.core.Component;
import org.example.engine.render.RenderContext;
import org.example.engine.render.pass.ComputeExamplePass;

public class ComputeExampleDisplay extends Component {
    private final ComputeExamplePass pass;

    public ComputeExampleDisplay(int width, int height) {
        pass = new ComputeExamplePass(width, height);
    }

    @Override
    public void render(RenderContext ctx) {
        pass.render(ctx);
    }

    @Override
    public boolean isRenderedByDefaultPipeline() {
        return false;
    }

    @Override
    public void dispose() {
        pass.dispose();
    }
}
