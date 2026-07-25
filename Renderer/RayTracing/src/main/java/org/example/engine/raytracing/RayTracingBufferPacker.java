package org.example.engine.raytracing;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.math.Vector3;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL33.GL_STATIC_DRAW;

public class RayTracingBufferPacker {
    public ComputeBuffer createMeshTriangleBuffer(List<RayTracingTriangle> triangles) {
        FloatBuffer data = MemoryUtil.memAllocFloat(triangles.size() * 12);
        for (RayTracingTriangle triangle : triangles) {
            putMeshTriangle(data, triangle);
        }
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(triangles.size(), RayTracingBufferLayout.MESH_TRIANGLE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createFullTriangleBuffer(List<RayTracingTriangle> triangles, List<RayTracingMaterialData> materials) {
        FloatBuffer data = MemoryUtil.memAllocFloat(triangles.size() * 24);
        for (int i = 0; i < triangles.size(); i++) {
            putPosition(data, triangles.get(i).p0);
            putPosition(data, triangles.get(i).p1);
            putPosition(data, triangles.get(i).p2);
            putMaterial(data, materials.get(i));
        }
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(triangles.size(), RayTracingBufferLayout.FULL_TRIANGLE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createMaterialBuffer(List<RayTracingMaterialData> materials) {
        FloatBuffer data = MemoryUtil.memAllocFloat(materials.size() * 12);
        for (RayTracingMaterialData material : materials) {
            putMaterial(data, material);
        }
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(materials.size(), RayTracingBufferLayout.MATERIAL_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createSphereBuffer(List<RayTracingSphereData> spheres) {
        FloatBuffer data = MemoryUtil.memAllocFloat(spheres.size() * 16);
        for (RayTracingSphereData sphere : spheres) {
            data.put(sphere.center.x).put(sphere.center.y).put(sphere.center.z).put(sphere.radius);
            putMaterial(data, sphere.material);
        }
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(spheres.size(), RayTracingBufferLayout.SPHERE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createNodeBuffer(List<RayTracingBvhBuilder.NodeData> nodes, RayTracingBounds fallbackBounds, int fallbackTriangleCount) {
        if (nodes == null || nodes.isEmpty()) {
            return createRootNodeBuffer(fallbackBounds, fallbackTriangleCount);
        }

        FloatBuffer data = MemoryUtil.memAllocFloat(nodes.size() * 16);
        for (RayTracingBvhBuilder.NodeData node : nodes) {
            data.put(node.bounds.min.x).put(node.bounds.min.y).put(node.bounds.min.z).put((float) node.triangleIndex);
            data.put(node.bounds.max.x).put(node.bounds.max.y).put(node.bounds.max.z).put((float) node.triangleSize);
            data.put((float) node.childAIndex).put((float) node.childBIndex).put(0.0f).put(0.0f);
            data.put(0.0f).put(0.0f).put(0.0f).put(0.0f);
        }
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(nodes.size(), RayTracingBufferLayout.NODE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createDummySphereBuffer() {
        FloatBuffer data = MemoryUtil.memAllocFloat(16);
        data.put(100000.0f).put(100000.0f).put(100000.0f).put(0.001f);
        putMaterial(data, RayTracingMaterialData.lambertian(new Vector3(0.0f)));
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(1, RayTracingBufferLayout.SPHERE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createDummyMeshTriangleBuffer() {
        FloatBuffer data = MemoryUtil.memAllocFloat(12);
        putMeshTriangle(data, new RayTracingTriangle(
                new Vector3(100000.0f, 100000.0f, 100000.0f),
                new Vector3(100001.0f, 100000.0f, 100000.0f),
                new Vector3(100000.0f, 100001.0f, 100000.0f)
        ));
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(1, RayTracingBufferLayout.MESH_TRIANGLE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createDummyFullTriangleBuffer() {
        FloatBuffer data = MemoryUtil.memAllocFloat(24);
        putPosition(data, new Vector3(100000.0f, 100000.0f, 100000.0f));
        putPosition(data, new Vector3(100001.0f, 100000.0f, 100000.0f));
        putPosition(data, new Vector3(100000.0f, 100001.0f, 100000.0f));
        putMaterial(data, RayTracingMaterialData.lambertian(new Vector3(0.0f)));
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(1, RayTracingBufferLayout.FULL_TRIANGLE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createRootNodeBuffer(RayTracingBounds bounds, int triangleCount) {
        FloatBuffer data = MemoryUtil.memAllocFloat(16);
        data.put(bounds.min.x).put(bounds.min.y).put(bounds.min.z).put(0.0f);
        data.put(bounds.max.x).put(bounds.max.y).put(bounds.max.z).put((float) triangleCount);
        data.put(0.0f).put(0.0f).put(0.0f).put(0.0f);
        data.put(0.0f).put(0.0f).put(0.0f).put(0.0f);
        data.flip();

        ComputeBuffer buffer = new ComputeBuffer(1, RayTracingBufferLayout.NODE_STRIDE, GL_STATIC_DRAW);
        buffer.setData(data);
        MemoryUtil.memFree(data);
        return buffer;
    }

    public ComputeBuffer createDummyNodeBuffer() {
        RayTracingBounds bounds = new RayTracingBounds();
        bounds.include(new Vector3(0.0f));
        return createRootNodeBuffer(bounds, 0);
    }

    private void putMeshTriangle(FloatBuffer data, RayTracingTriangle triangle) {
        data.put(triangle.p0.x).put(triangle.p0.y).put(triangle.p0.z).put((float) triangle.materialIndex);
        putPosition(data, triangle.p1);
        putPosition(data, triangle.p2);
    }

    private void putPosition(FloatBuffer data, Vector3 position) {
        data.put(position.x).put(position.y).put(position.z).put(0.0f);
    }

    private void putMaterial(FloatBuffer data, RayTracingMaterialData material) {
        data.put(material.type).put(0.0f).put(0.0f).put(0.0f);
        data.put(material.albedo.x).put(material.albedo.y).put(material.albedo.z).put(material.fuzz);
        data.put(material.refractionIndex).put(0.0f).put(0.0f).put(0.0f);
    }
}
