package org.example.engine.rendererPass;

import org.example.engine.gl.FBO;
import org.example.engine.material.*;
import org.example.engine.render.RenderContext;

import static org.lwjgl.opengl.GL33.*;

public class SSRPass extends RenderPass{

    SSRMaterial ssrMaterial;
    public SSRPass() {
        ssrMaterial = new SSRMaterial("/shaders/quad.frag", "/shaders/quad.vert");
    }

    @Override
    public void render(RenderContext ctx){

    }
}
