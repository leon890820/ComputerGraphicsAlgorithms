package org.example.engine.render;

import org.example.engine.gl.Texture;

public class RSMBuffer {
    public final Texture flux;
    public final Texture normal;
    public final Texture position;
    public final Texture depth;

    public RSMBuffer(Texture flux, Texture normal, Texture position, Texture depth) {
        this.flux = flux;
        this.normal = normal;
        this.position = position;
        this.depth = depth;
    }
}
