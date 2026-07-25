package org.example.engine.raytracing;

public final class RayTracingBufferLayout {
    public static final int MESH_TRIANGLE_STRIDE = 12 * Float.BYTES;
    public static final int FULL_TRIANGLE_STRIDE = 24 * Float.BYTES;
    public static final int MATERIAL_STRIDE = 12 * Float.BYTES;
    public static final int SPHERE_STRIDE = 16 * Float.BYTES;
    public static final int NODE_STRIDE = 16 * Float.BYTES;

    private RayTracingBufferLayout() {
    }
}
