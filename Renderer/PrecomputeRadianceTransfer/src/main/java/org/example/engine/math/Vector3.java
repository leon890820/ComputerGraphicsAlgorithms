package org.example.engine.math;

import java.util.Random;

public final class Vector3 {

    public float x;
    public float y;
    public float z;

    private static final Random RANDOM = new Random();

    public Vector3() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3(float _a) {
        x = _a;
        y = _a;
        z = _a;
    }

    public Vector3(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public float xyz(int i) {
        if (i == 0) return x;
        else if (i == 1) return y;
        else return z;
    }

    public static Vector3 Zero() {
        return new Vector3(0);
    }

    public static Vector3 Ones() {
        return new Vector3(1);
    }

    public static Vector3 UnitX() {
        return new Vector3(1, 0, 0);
    }

    public static Vector3 UnitY() {
        return new Vector3(0, 1, 0);
    }

    public static Vector3 UnitZ() {
        return new Vector3(0, 0, 1);
    }

    public void set(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public void setZero() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
    }

    public void setOnes() {
        x = 1.0f;
        y = 1.0f;
        z = 1.0f;
    }

    public void setUnitX() {
        x = 1.0f;
        y = 0.0f;
        z = 0.0f;
    }

    public void setUnitY() {
        x = 0.0f;
        y = 1.0f;
        z = 0.0f;
    }

    public void setUnitZ() {
        x = 0.0f;
        y = 0.0f;
        z = 1.0f;
    }

    public static Vector3 add(Vector3 a, Vector3 b) {
        return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vector3 sub(Vector3 a, Vector3 b) {
        return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vector3 mult(float n, Vector3 a) {
        return new Vector3(n * a.x, n * a.y, n * a.z);
    }

    public Vector3 mult(float n) {
        return new Vector3(n * x, n * y, n * z);
    }

    public void product(float n) {
        x *= n;
        y *= n;
        z *= n;
    }

    public Vector3 dive(float n) {
        return new Vector3(x / n, y / n, z / n);
    }

    public static Vector3 cross(Vector3 a, Vector3 b) {
        return new Vector3(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
        );
    }

    public static float dot(Vector3 a, Vector3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public float norm() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public void print() {
        System.out.println("x: " + x + " y: " + y + " z: " + z);
    }

    public Vector3 unit_vector() {
        float n = this.norm();
        if (n < 1e-8f) return new Vector3(0.0f);
        return Vector3.mult(1.0f / n, this);
    }

    public void normalize() {
        float n = this.norm();
        if (n < 1e-8f) return;
        this.product(1.0f / n);
    }

    public static Vector3 unit_vector(Vector3 v) {
        float n = v.norm();
        if (n < 1e-8f) return new Vector3(0.0f);
        return Vector3.mult(1.0f / n, v);
    }

    public Vector3 sub(Vector3 b) {
        return new Vector3(x - b.x, y - b.y, z - b.z);
    }

    public Vector3 add(Vector3 b) {
        return new Vector3(x + b.x, y + b.y, z + b.z);
    }

    public void minus(Vector3 b) {
        x -= b.x;
        y -= b.y;
        z -= b.z;
    }

    public void plus(Vector3 b) {
        x += b.x;
        y += b.y;
        z += b.z;
    }

    public float length_squared() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(this.length_squared());
    }

    public boolean near_zero() {
        float s = 1e-8f;
        return Math.abs(x) < s && Math.abs(y) < s && Math.abs(z) < s;
    }

    public Vector3 product(Vector3 v) {
        return new Vector3(x * v.x, y * v.y, z * v.z);
    }

    public Vector3 inv() {
        return new Vector3(1 / x, 1 / y, 1 / z);
    }

    public float magSq() {
        return x * x + y * y + z * z;
    }

    public void clipMag(float m) {
        float r = magSq() / (m * m);
        if (r > 1) {
            float sr = (float) Math.sqrt(r);
            x /= sr;
            y /= sr;
            z /= sr;
        }
    }

    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    public void copy(Vector3 b) {
        x = b.x;
        y = b.y;
        z = b.z;
    }

    public Vector4 getVector4() {
        return new Vector4(this, 1);
    }

    public Vector4 getVector4(float b) {
        return new Vector4(this, b);
    }

    public static Vector3 random_unit_sphere_vector() {
        float phi = random(0, 1) * 2.0f * (float) Math.PI;
        float theta = (float) Math.acos(-2.0f * random(-0.5f, 0.5f));
        return toVector(phi, theta);
    }

    public static Vector3 vec_random() {
        return new Vector3(random(1), random(1), random(1));
    }

    public static Vector3 vec_random(float min, float max) {
        return new Vector3(random(min, max), random(min, max), random(min, max));
    }

    public static Vector3 random_in_unit_sphere() {
        while (true) {
            Vector3 p = vec_random(-1, 1);
            if (p.length_squared() >= 1) continue;
            return p;
        }
    }

    public static Vector3 random_unit_vector() {
        return Vector3.unit_vector(random_in_unit_sphere());
    }

    public static Vector3 reflect(Vector3 v, Vector3 n) {
        Vector3 r = n.mult(2 * Vector3.dot(v, n));
        return v.sub(r);
    }

    public static Vector3 refract(Vector3 uv, Vector3 n, float etai_over_etat) {
        float cosTheta = Math.min(Vector3.dot(uv.mult(-1), n), 1);
        Vector3 rOutPerp = uv.add(n.mult(cosTheta)).mult(etai_over_etat);
        Vector3 rOutParallel = n.mult(-(float) Math.sqrt(1 - rOutPerp.length_squared()));
        return rOutPerp.add(rOutParallel);
    }

    public static Vector3 random_in_unit_disk() {
        while (true) {
            Vector3 p = new Vector3(random(-1, 1), random(-1, 1), 0);
            if (p.length_squared() >= 1) continue;
            return p;
        }
    }

    public static Vector3 toVector(float phi, float theta) {
        float sinTheta = (float) Math.sin(theta);
        return new Vector3(
                sinTheta * (float) Math.cos(phi),
                sinTheta * (float) Math.sin(phi),
                (float) Math.cos(theta)
        );
    }

    private static float random(float max) {
        return RANDOM.nextFloat() * max;
    }

    private static float random(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    @Override
    public String toString() {
        return "x : " + x + " y : " + y + " z : " + z;
    }
}