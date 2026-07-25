package org.example.engine.raytracing;

import org.example.engine.math.Vector3;

public class RayTracingTriangle {
    public final Vector3 p0;
    public final Vector3 p1;
    public final Vector3 p2;
    public final Vector3 uv0;
    public final Vector3 uv1;
    public final Vector3 uv2;
    public final int materialIndex;

    public RayTracingTriangle(Vector3 p0, Vector3 p1, Vector3 p2) {
        this(p0, p1, p2, 0);
    }

    public RayTracingTriangle(Vector3 p0, Vector3 p1, Vector3 p2, int materialIndex) {
        this(p0, p1, p2, new Vector3(0.0f), new Vector3(0.0f), new Vector3(0.0f), materialIndex);
    }

    public RayTracingTriangle(
            Vector3 p0,
            Vector3 p1,
            Vector3 p2,
            Vector3 uv0,
            Vector3 uv1,
            Vector3 uv2,
            int materialIndex
    ) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.uv0 = uv0 == null ? new Vector3(0.0f) : uv0;
        this.uv1 = uv1 == null ? new Vector3(0.0f) : uv1;
        this.uv2 = uv2 == null ? new Vector3(0.0f) : uv2;
        this.materialIndex = Math.max(0, materialIndex);
    }

    public RayTracingTriangle withMaterialIndex(int materialIndex) {
        return new RayTracingTriangle(p0, p1, p2, uv0, uv1, uv2, materialIndex);
    }

    public Vector3 center() {
        return p0.add(p1).add(p2).mult(1.0f / 3.0f);
    }
}
