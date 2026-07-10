package org.example.engine.render;

import org.example.engine.render.pass.SkyBoxPass;
import org.example.engine.render.pass.PRTPass;

public class Renderer {
    private final SkyBoxPass skyBoxPass;
    private final PRTPass prtPass;


    public Renderer(int screenWidth, int screenHeight) {
        skyBoxPass = new SkyBoxPass();
        prtPass = new PRTPass();
    }

    public void render(RenderContext ctx) {
        skyBoxPass.render(ctx);
        prtPass.render(ctx);
    }
}
