package org.example.engine.material;

import org.example.engine.math.Vector3;

public class DebugLineMaterial extends Material {
    private Vector3 colour = new Vector3(0.25f, 1.0f, 0.45f);

    public DebugLineMaterial() {
        super("/shaders/debug_line.frag", "/shaders/debug_line.vert");
    }

    public DebugLineMaterial setColour(float r, float g, float b) {
        colour = new Vector3(r, g, b);
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.mvpMatrix == null) {
            return;
        }

        setMatrix4ToUniform("uMVP", data.mvpMatrix);
        setVector3ToUniform("uColour", colour);
    }
}
