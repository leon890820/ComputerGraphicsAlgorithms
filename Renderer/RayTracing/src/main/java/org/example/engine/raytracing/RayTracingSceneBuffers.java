package org.example.engine.raytracing;

import org.example.engine.gl.ComputeBuffer;

public class RayTracingSceneBuffers {
    public final ComputeBuffer triangleBuffer;
    public final ComputeBuffer sphereBuffer;
    public final ComputeBuffer nodeBuffer;
    public final ComputeBuffer cornellBoxBuffer;
    public final ComputeBuffer materialBuffer;
    public final int triangleCount;
    public final int sphereCount;
    public final int cornellTriangleCount;
    public final int materialCount;

    public RayTracingSceneBuffers(
            ComputeBuffer triangleBuffer,
            int triangleCount,
            ComputeBuffer sphereBuffer,
            int sphereCount,
            ComputeBuffer nodeBuffer,
            ComputeBuffer cornellBoxBuffer,
            int cornellTriangleCount,
            ComputeBuffer materialBuffer,
            int materialCount
    ) {
        this.triangleBuffer = triangleBuffer;
        this.triangleCount = Math.max(0, triangleCount);
        this.sphereBuffer = sphereBuffer;
        this.sphereCount = Math.max(0, sphereCount);
        this.nodeBuffer = nodeBuffer;
        this.cornellBoxBuffer = cornellBoxBuffer;
        this.cornellTriangleCount = Math.max(0, cornellTriangleCount);
        this.materialBuffer = materialBuffer;
        this.materialCount = Math.max(0, materialCount);
    }

    public void dispose() {
        dispose(triangleBuffer);
        dispose(sphereBuffer);
        dispose(nodeBuffer);
        dispose(cornellBoxBuffer);
        dispose(materialBuffer);
    }

    private void dispose(ComputeBuffer buffer) {
        if (buffer != null) {
            buffer.dispose();
        }
    }
}
