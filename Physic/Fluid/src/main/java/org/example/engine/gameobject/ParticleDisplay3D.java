package org.example.engine.gameobject;

import org.example.engine.component.MeshRenderer;
import org.example.engine.gl.ComputeBuffer;
import org.example.engine.material.Particle3DMaterial;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SphereGenerator;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class ParticleDisplay3D extends GameObject {
    private static final int FLOATS_PER_PARTICLE = 4;
    private static final int PARTICLE_STRIDE = FLOATS_PER_PARTICLE * Float.BYTES;

    private final int particleCount;
    private final ComputeBuffer positionBuffer;
    private final ComputeBuffer velocityBuffer;
    private final Particle3DMaterial material;

    public ParticleDisplay3D(int gridSize, float spacing, int meshResolution, float particleScale) {
        particleCount = gridSize * gridSize * gridSize;
        positionBuffer = new ComputeBuffer(particleCount, PARTICLE_STRIDE);
        velocityBuffer = new ComputeBuffer(particleCount, PARTICLE_STRIDE);

        uploadInitialGrid(gridSize, spacing);
        uploadZeroVelocities();

        material = new Particle3DMaterial(positionBuffer, velocityBuffer)
                .setScale(particleScale)
                .setVelocityMax(1.0f);

        Mesh mesh = SphereGenerator.generateSphereMesh(meshResolution);
        setMesh(mesh);
        buildSubMeshRenderers(material);
    }

    public ComputeBuffer getPositionBuffer() {
        return positionBuffer;
    }

    public ComputeBuffer getVelocityBuffer() {
        return velocityBuffer;
    }

    public int getParticleCount() {
        return particleCount;
    }

    @Override
    public void run(RenderContext ctx) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.renderInstanced(createMaterialRenderData(ctx, mr), material, particleCount);
            }
        }
    }

    @Override
    public void dispose(ResourceDisposalContext disposalContext) {
        super.dispose(disposalContext);
        positionBuffer.dispose();
        velocityBuffer.dispose();
    }

    private void uploadInitialGrid(int gridSize, float spacing) {
        FloatBuffer data = MemoryUtil.memAllocFloat(particleCount * FLOATS_PER_PARTICLE);
        float centerOffset = (gridSize - 1) * 0.5f;

        for (int z = 0; z < gridSize; z++) {
            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    data.put((x - centerOffset) * spacing);
                    data.put((y - centerOffset) * spacing);
                    data.put((z - centerOffset) * spacing);
                    data.put(1.0f);
                }
            }
        }

        data.flip();
        positionBuffer.setData(data);
        MemoryUtil.memFree(data);
    }

    private void uploadZeroVelocities() {
        FloatBuffer data = MemoryUtil.memAllocFloat(particleCount * FLOATS_PER_PARTICLE);

        for (int i = 0; i < particleCount; i++) {
            data.put(0.0f);
            data.put(0.0f);
            data.put(0.0f);
            data.put(0.0f);
        }

        data.flip();
        velocityBuffer.setData(data);
        MemoryUtil.memFree(data);
    }
}