package org.example.engine.render.pass;

import org.example.engine.component.render.MeshRenderer;
import org.example.engine.gl.FBO;
import org.example.engine.gl.Texture;
import org.example.engine.material.EdgeMaterial;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class TriangleEdgePass extends RenderPass {

    private final FBO edgeBuffer;
    private final EdgeMaterial edgeMaterial;

    public TriangleEdgePass(int width, int height) {
        edgeBuffer = new FBO(width, height, 1, GL_NEAREST, false);
        edgeMaterial = new EdgeMaterial("/shaders/triangleEdge.frag", "/shaders/triangleEdge.vert");
    }

    public Texture getEdgeTexture() {
        return edgeBuffer.getColorTexture(0);
    }

    public void render(RenderContext ctx) {
        if (ctx == null || ctx.scene == null) {
            return;
        }

        edgeBuffer.bindFrameBuffer();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glLineWidth(1.0f);
        try {
            for (MeshRenderer renderer : ctx.scene.getComponents(MeshRenderer.class)) {
                if (renderer.isEnabled() && renderer.isRenderedByDefaultPipeline()) {
                    renderer.render(ctx, edgeMaterial);
                }
            }
        } finally {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        edgeBuffer.unbindFrameBuffer(ctx.screenWidth, ctx.screenHeight);
    }
}
