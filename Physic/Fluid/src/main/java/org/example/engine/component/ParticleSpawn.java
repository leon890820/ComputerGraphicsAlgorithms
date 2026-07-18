package org.example.engine.component;

import java.nio.FloatBuffer;

public class ParticleSpawn {
    private static final int DEFAULT_GRID_SIZE = 25;
    private static final float DEFAULT_SPAWN_SPACING = 0.04f;
    private static final int DEFAULT_MESH_RESOLUTION = 3;
    private static final float DEFAULT_PARTICLE_RADIUS = 0.01f;

    private int gridSize = DEFAULT_GRID_SIZE;
    private float spawnSpacing = DEFAULT_SPAWN_SPACING;
    private int meshResolution = DEFAULT_MESH_RESOLUTION;
    private float particleRadius = DEFAULT_PARTICLE_RADIUS;

    public int getParticleCount() {
        return gridSize * gridSize * gridSize;
    }

    public int getMeshResolution() {
        return meshResolution;
    }

    public float getParticleRadius() {
        return particleRadius;
    }

    public ParticleSpawn setGridSize(int gridSize) {
        this.gridSize = Math.max(1, gridSize);
        return this;
    }

    public ParticleSpawn setSpawnSpacing(float spawnSpacing) {
        this.spawnSpacing = Math.max(0.0f, spawnSpacing);
        return this;
    }

    public ParticleSpawn setMeshResolution(int meshResolution) {
        this.meshResolution = Math.max(0, meshResolution);
        return this;
    }

    public ParticleSpawn setParticleRadius(float particleRadius) {
        this.particleRadius = Math.max(0.0f, particleRadius);
        return this;
    }

    public void writeSpawnPositions(FloatBuffer data) {
        float centerOffset = (gridSize - 1) * 0.5f;

        for (int z = 0; z < gridSize; z++) {
            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    data.put((x - centerOffset) * spawnSpacing);
                    data.put((y - centerOffset) * spawnSpacing);
                    data.put((z - centerOffset) * spawnSpacing);
                    data.put(1.0f);
                }
            }
        }
    }
}
