package org.example.engine.render;

import org.example.engine.render.pass.SkyBoxPass;

public class Renderer {
    private final SkyBoxPass skyBoxPass;


    public Renderer(int screenWidth, int screenHeight) {
        skyBoxPass = new SkyBoxPass();
    }

    public void render(RenderContext ctx) {
        skyBoxPass.render(ctx);
    }
}
