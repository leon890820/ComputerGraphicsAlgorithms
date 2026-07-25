package org.example.engine.raytracing;

import org.example.engine.math.Vector3;

public class RayTracingMaterialData {
    public final float type;
    public final Vector3 albedo;
    public final float fuzz;
    public final float refractionIndex;
    public final int textureIndex;

    public RayTracingMaterialData(float type, Vector3 albedo, float fuzz, float refractionIndex) {
        this(type, albedo, fuzz, refractionIndex, -1);
    }

    public RayTracingMaterialData(float type, Vector3 albedo, float fuzz, float refractionIndex, int textureIndex) {
        this.type = type;
        this.albedo = albedo == null ? new Vector3(0.0f) : albedo;
        this.fuzz = fuzz;
        this.refractionIndex = refractionIndex;
        this.textureIndex = textureIndex;
    }

    public RayTracingMaterialData withTextureIndex(int textureIndex) {
        return new RayTracingMaterialData(type, albedo, fuzz, refractionIndex, textureIndex);
    }

    public static RayTracingMaterialData lambertian(Vector3 albedo) {
        return new RayTracingMaterialData(0.0f, albedo, 0.0f, 1.5f);
    }

    public static RayTracingMaterialData metal(Vector3 albedo, float fuzz) {
        return new RayTracingMaterialData(1.0f, albedo, fuzz, 1.5f);
    }

    public static RayTracingMaterialData dielectric(Vector3 albedo, float fuzz, float refractionIndex) {
        return new RayTracingMaterialData(2.0f, albedo, fuzz, refractionIndex);
    }

    public static RayTracingMaterialData emissive(Vector3 color) {
        return new RayTracingMaterialData(3.0f, color, 0.0f, 1.5f);
    }
}
