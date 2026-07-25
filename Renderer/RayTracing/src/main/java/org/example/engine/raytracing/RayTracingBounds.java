package org.example.engine.raytracing;

import org.example.engine.math.Vector3;

public class RayTracingBounds {
    public final Vector3 min = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    public final Vector3 max = new Vector3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

    public void include(Vector3 p) {
        min.x = Math.min(min.x, p.x);
        min.y = Math.min(min.y, p.y);
        min.z = Math.min(min.z, p.z);
        max.x = Math.max(max.x, p.x);
        max.y = Math.max(max.y, p.y);
        max.z = Math.max(max.z, p.z);
    }

    public void include(RayTracingTriangle triangle) {
        include(triangle.p0);
        include(triangle.p1);
        include(triangle.p2);
    }

    public void include(RayTracingBounds bounds) {
        if (bounds == null || !bounds.isValid()) {
            return;
        }
        include(bounds.min);
        include(bounds.max);
    }

    public boolean isValid() {
        return min.x <= max.x && min.y <= max.y && min.z <= max.z;
    }

    public Vector3 center() {
        return min.add(max).mult(0.5f);
    }

    public Vector3 size() {
        return max.sub(min);
    }
}
