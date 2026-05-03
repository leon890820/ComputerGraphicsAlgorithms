package org.example.engine.light;

import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;

public class SpotLight extends Light {

    public float fov = 60.0f;
    public float aspect = 1.0f;
    public float near = 0.1f;
    public float far = 10000.0f;

    public float cutoff = 12.5f;
    public float outerCutoff = 17.5f;

    public SpotLight(Vector3 pos, Vector3 dir, Vector3 c) {
        super(pos, dir, c);
    }

    @Override
    public Light setPerspective(float fov, float aspect, float near, float far) {
        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
        return this;
    }

    public SpotLight setCutoff(float cutoff, float outerCutoff) {
        this.cutoff = cutoff;
        this.outerCutoff = outerCutoff;
        return this;
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return Matrix4.Perspective(fov, aspect, near, far);
    }

    @Override
    public float getLightFar() {
        return far;
    }

}