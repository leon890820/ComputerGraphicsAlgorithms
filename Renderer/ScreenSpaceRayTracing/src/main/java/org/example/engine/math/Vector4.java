package org.example.engine.math;

public class Vector4 {

    public float x;
    public float y;
    public float z;
    public float w;

    public Vector4() {
    }

    public Vector4(Vector3 xyz, float w) {
        this.x = xyz.x;
        this.y = xyz.y;
        this.z = xyz.z;
        this.w = w;
    }

    public Vector4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4(float x) {
        this.x = x;
        this.y = x;
        this.z = x;
        this.w = x;
    }

    public Vector3 xyz() {
        return new Vector3(x, y, z);
    }

    public Vector3 xyzNormalize() {
        return xyz().unit_vector();
    }

    public Vector3 homogenized() {
        if (w < 0) return new Vector3(-x / w, -y / w, -z / w);
        return new Vector3(x / w, y / w, z / w);
    }

    public Vector4 reverseW() {
        return new Vector4(x, y, z, -Math.abs(w));
    }

    public void set(float _x, float _y, float _z, float _w) {
        x = _x;
        y = _y;
        z = _z;
        w = _w;
    }

    public Vector4 add(Vector4 v) {
        return new Vector4(x + v.x, y + v.y, z + v.z, w);
    }

    public Vector4 mult(float b) {
        return new Vector4(x * b, y * b, z * b, w);
    }

    public Vector4 mult(Matrix4 m) {
        return new Vector4(
                m.m[0] * x + m.m[1] * y + m.m[2] * z + m.m[3] * w,
                m.m[4] * x + m.m[5] * y + m.m[6] * z + m.m[7] * w,
                m.m[8] * x + m.m[9] * y + m.m[10] * z + m.m[11] * w,
                m.m[12] * x + m.m[13] * y + m.m[14] * z + m.m[15] * w
        );
    }

    public void multiply(float b) {
        x *= b;
        y *= b;
        z *= b;
    }

    public Vector4 div(float b) {
        return new Vector4(x / b, y / b, z / b, w);
    }

    public void dive(float b) {
        x /= b;
        y /= b;
        z /= b;
        w /= b;
    }

    public float dot(Vector4 b) {
        return x * b.x + y * b.y + z * b.z;
    }

    public float dot(Vector3 b) {
        return x * b.x + y * b.y + z * b.z;
    }

    @Override
    public String toString() {
        return "x : " + x + " y : " + y + " z : " + z + " w : " + w;
    }
}