package org.example.engine.component;

import org.example.engine.gl.ComputeBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class ParticleBuffer {
    private static final int FLOATS_PER_PARTICLE = 4;
    private static final int PARTICLE_STRIDE = FLOATS_PER_PARTICLE * Float.BYTES;

    private final int particleCount;
    private final ComputeBuffer positionBuffer;
    private final ComputeBuffer predictedPositionBuffer;
    private final ComputeBuffer velocityBuffer;
    private final ComputeBuffer densityBuffer;
    private final ComputeBuffer spatialIndexBuffer;
    private final ComputeBuffer spatialOffsetBuffer;

    public ParticleBuffer(ParticleSpawn spawn) {
        ParticleSpawn particleSpawn = spawn == null ? new ParticleSpawn() : spawn;

        particleCount = particleSpawn.getParticleCount();
        positionBuffer = new ComputeBuffer(particleCount, PARTICLE_STRIDE);
        predictedPositionBuffer = new ComputeBuffer(particleCount, PARTICLE_STRIDE);
        velocityBuffer = new ComputeBuffer(particleCount, PARTICLE_STRIDE);
        densityBuffer = new ComputeBuffer(particleCount, PARTICLE_STRIDE);
        spatialIndexBuffer = new ComputeBuffer(particleCount, GPUSort.ENTRY_STRIDE);
        spatialOffsetBuffer = new ComputeBuffer(particleCount, Integer.BYTES);

        uploadInitialSpawn(particleSpawn);
        uploadZeroVelocities();
        uploadZeroDensities();
        uploadInitialSpatialOffsets();
    }

    public ComputeBuffer getPositionBuffer() {
        return positionBuffer;
    }

    public ComputeBuffer getPredictedPositionBuffer() {
        return predictedPositionBuffer;
    }

    public ComputeBuffer getVelocityBuffer() {
        return velocityBuffer;
    }

    public ComputeBuffer getDensityBuffer() {
        return densityBuffer;
    }

    public ComputeBuffer getSpatialIndexBuffer() {
        return spatialIndexBuffer;
    }

    public ComputeBuffer getSpatialOffsetBuffer() {
        return spatialOffsetBuffer;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void dispose() {
        positionBuffer.dispose();
        predictedPositionBuffer.dispose();
        velocityBuffer.dispose();
        densityBuffer.dispose();
        spatialIndexBuffer.dispose();
        spatialOffsetBuffer.dispose();
    }

    private void uploadInitialSpawn(ParticleSpawn spawn) {
        FloatBuffer data = MemoryUtil.memAllocFloat(particleCount * FLOATS_PER_PARTICLE);
        spawn.writeSpawnPositions(data);

        data.flip();
        positionBuffer.setData(data);
        data.rewind();
        predictedPositionBuffer.setData(data);
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

    private void uploadZeroDensities() {
        FloatBuffer data = MemoryUtil.memAllocFloat(particleCount * FLOATS_PER_PARTICLE);

        for (int i = 0; i < particleCount; i++) {
            data.put(0.0f);
            data.put(0.0f);
            data.put(0.0f);
            data.put(0.0f);
        }

        data.flip();
        densityBuffer.setData(data);
        MemoryUtil.memFree(data);
    }

    private void uploadInitialSpatialOffsets() {
        int[] data = new int[particleCount];

        for (int i = 0; i < particleCount; i++) {
            data[i] = particleCount;
        }

        spatialOffsetBuffer.setData(data);
    }
}
