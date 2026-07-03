package org.example.engine.render;

import org.example.engine.gl.TextureCube;

public class RSMCubeBuffer {
    public final TextureCube flux;
    public final TextureCube normal;
    public final TextureCube position;
    public final TextureCube depth;

    public RSMCubeBuffer(TextureCube flux, TextureCube normal, TextureCube position, TextureCube depth) {
        this.flux = flux;
        this.normal = normal;
        this.position = position;
        this.depth = depth;
    }
}
