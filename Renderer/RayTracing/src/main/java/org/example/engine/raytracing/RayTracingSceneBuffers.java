package org.example.engine.raytracing;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.Texture;

import java.util.Collections;
import java.util.List;

public class RayTracingSceneBuffers {
    public final ComputeBuffer triangleBuffer;
    public final ComputeBuffer sphereBuffer;
    public final ComputeBuffer nodeBuffer;
    public final ComputeBuffer cornellBoxBuffer;
    public final ComputeBuffer materialBuffer;
    public final List<Texture> diffuseTextures;
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
        this(
                triangleBuffer,
                triangleCount,
                sphereBuffer,
                sphereCount,
                nodeBuffer,
                cornellBoxBuffer,
                cornellTriangleCount,
                materialBuffer,
                materialCount,
                Collections.emptyList()
        );
    }

    public RayTracingSceneBuffers(
            ComputeBuffer triangleBuffer,
            int triangleCount,
            ComputeBuffer sphereBuffer,
            int sphereCount,
            ComputeBuffer nodeBuffer,
            ComputeBuffer cornellBoxBuffer,
            int cornellTriangleCount,
            ComputeBuffer materialBuffer,
            int materialCount,
            List<Texture> diffuseTextures
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
        this.diffuseTextures = diffuseTextures == null ? Collections.emptyList() : diffuseTextures;
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
