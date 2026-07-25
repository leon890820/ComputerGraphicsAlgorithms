package org.example.engine.raytracing;

import org.example.engine.math.Matrix4;

public class RayTracingMeshInstance {
    public final String meshPath;
    public final Matrix4 transform;
    public final RayTracingMaterialData material;
    public final boolean normalizeToTargetSize;
    public final float targetSize;

    public RayTracingMeshInstance(String meshPath, Matrix4 transform, RayTracingMaterialData material) {
        this(meshPath, transform, material, false, 1.0f);
    }

    public RayTracingMeshInstance(
            String meshPath,
            Matrix4 transform,
            RayTracingMaterialData material,
            boolean normalizeToTargetSize,
            float targetSize
    ) {
        this.meshPath = meshPath;
        this.transform = transform == null ? Matrix4.Identity() : transform;
        this.material = material == null ? RayTracingMaterialData.lambertian(null) : material;
        this.normalizeToTargetSize = normalizeToTargetSize;
        this.targetSize = Math.max(0.0001f, targetSize);
    }

    public static RayTracingMeshInstance normalized(String meshPath, Matrix4 transform, RayTracingMaterialData material, float targetSize) {
        return new RayTracingMeshInstance(meshPath, transform, material, true, targetSize);
    }
}
