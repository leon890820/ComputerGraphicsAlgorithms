package org.example.engine.raytracing;

import org.example.engine.math.Vector3;

public class RayTracingSphereData {
    public final Vector3 center;
    public final float radius;
    public final RayTracingMaterialData material;

    public RayTracingSphereData(Vector3 center, float radius, RayTracingMaterialData material) {
        this.center = center == null ? new Vector3(0.0f) : center;
        this.radius = Math.max(0.0001f, radius);
        this.material = material == null ? RayTracingMaterialData.lambertian(new Vector3(0.0f)) : material;
    }
}
