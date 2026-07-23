package org.example.engine.material;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.math.Vector3;

public class Particle3DMaterial extends Material {
    private final ComputeBuffer positions;
    private final ComputeBuffer velocities;

    private float scale = 0.05f;
    private float velocityMax = 1.0f;
    private Vector3 colour = new Vector3(0.0f, 1.0f, 0.74f);
    private Vector3 rimColour = new Vector3(0.45f, 1.0f, 0.82f);
    private Vector3 lightDirection = new Vector3(0.4f, 0.8f, 0.3f).unit_vector();

    public Particle3DMaterial(ComputeBuffer positions, ComputeBuffer velocities) {
        super("/shaders/particle/shader/particle3d.frag", "/shaders/particle/shader/particle3d.vert");
        this.positions = positions;
        this.velocities = velocities;
    }

    public Particle3DMaterial setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public Particle3DMaterial setVelocityMax(float velocityMax) {
        this.velocityMax = velocityMax;
        return this;
    }

    public Particle3DMaterial setColour(float r, float g, float b) {
        colour = new Vector3(r, g, b);
        return this;
    }

    public Particle3DMaterial setRimColour(float r, float g, float b) {
        rimColour = new Vector3(r, g, b);
        return this;
    }

    public Particle3DMaterial setLightDirection(Vector3 lightDirection) {
        if (lightDirection != null) {
            this.lightDirection = lightDirection.unit_vector();
        }
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        positions.bindBase(0);
        velocities.bindBase(1);

        setMatrix4ToUniform("uMVP", data.mvpMatrix);
        setMatrix4ToUniform("uLocalToWorld", data.modelMatrix);
        setFloatToUniform("uScale", scale);
        setFloatToUniform("uVelocityMax", Math.max(velocityMax, 0.0001f));
        setVector3ToUniform("uColour", colour);
        setVector3ToUniform("uRimColour", rimColour);
        setVector3ToUniform("uLightDir", lightDirection);
        setVector3ToUniform("uCameraPosition", data.viewPosition);
    }
}
