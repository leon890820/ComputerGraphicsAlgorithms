package org.example.engine.raytracing;

import org.example.engine.math.Vector3;

public class RayTracingTriangle {
    public final Vector3 p0;
    public final Vector3 p1;
    public final Vector3 p2;
    public final int materialIndex;

    public RayTracingTriangle(Vector3 p0, Vector3 p1, Vector3 p2) {
        this(p0, p1, p2, 0);
    }

    public RayTracingTriangle(Vector3 p0, Vector3 p1, Vector3 p2, int materialIndex) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.materialIndex = Math.max(0, materialIndex);
    }

    public RayTracingTriangle withMaterialIndex(int materialIndex) {
        return new RayTracingTriangle(p0, p1, p2, materialIndex);
    }

    public Vector3 center() {
        return p0.add(p1).add(p2).mult(1.0f / 3.0f);
    }
}
