package org.example.engine.render;

import org.example.engine.gl.Texture;

public class GBuffer {
    public final Texture albedo;
    public final Texture normal;
    public final Texture position;
    public final Texture viewDepth;
    public final Texture depth;

    public GBuffer(Texture albedo, Texture normal, Texture position, Texture viewDepth, Texture depth) {
        this.albedo = albedo;
        this.normal = normal;
        this.position = position;
        this.viewDepth = viewDepth;
        this.depth = depth;
    }
}
