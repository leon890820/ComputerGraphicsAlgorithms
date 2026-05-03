package org.example.engine.light;

import org.example.engine.math.*;

public class DirectionalLight extends Light {

    public float left = -5.0f;
    public float right = 5.0f;
    public float bottom = -5.0f;
    public float top = 5.0f;
    public float near = 0.01f;
    public float far = 200.0f;

    public DirectionalLight(Vector3 pos, Vector3 dir, Vector3 c) {
        super(pos, dir, c);
    }

    @Override
    public Light setOrtho(float left, float right, float bottom, float top, float near, float far) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;
        return this;
    }



    @Override
    public Matrix4 getProjectionMatrix() {
        return Matrix4.Ortho(left, right, bottom, top, near, far);
    }

    @Override
    public float getLightFar(){
        return far;
    }

}
