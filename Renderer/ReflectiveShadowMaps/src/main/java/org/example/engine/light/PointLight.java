package org.example.engine.light;
import org.example.engine.math.*;

public class PointLight extends Light {

    float radius = 10.0f;
    float near = 0.1f;
    float far = 10000.0f;
    float intensity = 1.0f;

    public PointLight(Vector3 pos, Vector3 c) {
        super(pos, new Vector3(0, 0, 0), c);
    }

    public PointLight setRadius(float r) {
        radius = r;
        return this;
    }

    public PointLight setIntensity(float i) {
        intensity = i;
        return this;
    }

    public PointLight setNearFar(float n, float f) {
        near = n;
        far = f;
        return this;
    }

    public float getRadius() {
        return radius;
    }

    public float getIntensity() {
        return intensity;
    }

    public Vector3 getPosition() {
        return transform.position;
    }

    public Matrix4[] getShadowMatrices() {
        Vector3 pos = transform.position;
        Matrix4 proj = Matrix4.Perspective(90.0f, 1.0f, near, far);

        Matrix4[] mats = new Matrix4[6];

        mats[0] = proj.mult(Matrix4.LookAt(pos, pos.add(new Vector3( 1,  0,  0)), new Vector3(0, -1,  0)));
        mats[1] = proj.mult(Matrix4.LookAt(pos, pos.add(new Vector3(-1,  0,  0)), new Vector3(0, -1,  0)));
        mats[2] = proj.mult(Matrix4.LookAt(pos, pos.add(new Vector3( 0,  1,  0)), new Vector3(0,  0,  1)));
        mats[3] = proj.mult(Matrix4.LookAt(pos, pos.add(new Vector3( 0, -1,  0)), new Vector3(0,  0, -1)));
        mats[4] = proj.mult(Matrix4.LookAt(pos, pos.add(new Vector3( 0,  0,  1)), new Vector3(0, -1,  0)));
        mats[5] = proj.mult(Matrix4.LookAt(pos, pos.add(new Vector3( 0,  0, -1)), new Vector3(0, -1,  0)));

        return mats;
    }

    @Override
    public Matrix4 getViewMatrix() {
        return Matrix4.Identity();
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return Matrix4.Identity();
    }


    public float getLightFar(){
        return far;
    }


}
