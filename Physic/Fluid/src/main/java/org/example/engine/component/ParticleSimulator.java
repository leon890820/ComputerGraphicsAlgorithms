package org.example.engine.component;

import org.example.engine.gl.ComputeHelper;
import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.ComputeShader;
import org.example.engine.math.Vector3;

public class ParticleSimulator {
    private final ComputeShader externalForcesShader;
    private final ComputeShader spatialHashShader;
    private final ComputeShader densityShader;
    private final ComputeShader pressureShader;
    private final ComputeShader viscosityShader;
    private final ComputeShader updatePositionsShader;
    private final GPUSort gpuSort;
    private final ParticleCollider collider;
    private final ComputeBuffer colliderBuffer;

    private float deltaTime = 1.0f / 60.0f;
    private float timeScale = 1.0f;
    private int iterationsPerFrame = 2;
    private Vector3 gravity = new Vector3(0.0f, -9.8f, 0.0f);
    private float smoothingRadius = 0.2f;
    private float targetDensity = 10.0f;
    private float pressureMultiplier = 88.0f;
    private float nearPressureMultiplier = 2.25f;
    private float viscosityStrength = 0.001f;
    private Vector3 center = new Vector3(0.0f, 0.0f, 0.0f);
    private float concentration = 0.0f;
    private float bounceDamping = 0.99f;

    public ParticleSimulator() {
        externalForcesShader = new ComputeShader("/shaders/particle_external_forces.comp");
        spatialHashShader = new ComputeShader("/shaders/particle_spatial_hash.comp");
        densityShader = new ComputeShader("/shaders/particle_calculate_densities.comp");
        pressureShader = new ComputeShader("/shaders/particle_pressure.comp");
        viscosityShader = new ComputeShader("/shaders/particle_viscosity.comp");
        updatePositionsShader = new ComputeShader("/shaders/particle_update.comp");
        gpuSort = new GPUSort();
        collider = new ParticleCollider();
        colliderBuffer = ComputeBuffer.fromFloats(collider.toBufferData(), ParticleCollider.COLLIDER_STRIDE);
    }

    public void update(ParticleBuffer buffer) {
        if (buffer == null || buffer.getParticleCount() <= 0) {
            return;
        }

        float stepDeltaTime = deltaTime / Math.max(1, iterationsPerFrame) * timeScale;

        for (int i = 0; i < iterationsPerFrame; i++) {
            runSimulationStep(buffer, stepDeltaTime);
        }
    }

    public void runSimulationStep(ParticleBuffer buffer) {
        runSimulationStep(buffer, deltaTime);
    }

    public void runSimulationStep(ParticleBuffer buffer, float stepDeltaTime) {
        if (buffer == null || buffer.getParticleCount() <= 0) {
            return;
        }

        runExternalForces(buffer, stepDeltaTime);
        runSpatialHash(buffer);
        gpuSort.setBuffers(buffer.getSpatialIndexBuffer(), buffer.getSpatialOffsetBuffer());
        gpuSort.sortAndCalculateOffsets();
        runDensity(buffer);
        runPressure(buffer, stepDeltaTime);
        runViscosity(buffer, stepDeltaTime);
        runUpdatePositions(buffer, stepDeltaTime);
    }

    public ParticleSimulator setDeltaTime(float deltaTime) {
        this.deltaTime = Math.max(0.0f, deltaTime);
        return this;
    }

    public ParticleSimulator setTimeScale(float timeScale) {
        this.timeScale = Math.max(0.0f, timeScale);
        return this;
    }

    public ParticleSimulator setIterationsPerFrame(int iterationsPerFrame) {
        this.iterationsPerFrame = Math.max(1, iterationsPerFrame);
        return this;
    }

    public ParticleSimulator setGravity(Vector3 gravity) {
        if (gravity != null) {
            this.gravity = gravity;
        }
        return this;
    }

    public ParticleSimulator setSmoothingRadius(float smoothingRadius) {
        this.smoothingRadius = Math.max(0.0001f, smoothingRadius);
        return this;
    }

    public ParticleSimulator setPressure(float targetDensity, float pressureMultiplier, float nearPressureMultiplier) {
        this.targetDensity = Math.max(0.0001f, targetDensity);
        this.pressureMultiplier = pressureMultiplier;
        this.nearPressureMultiplier = nearPressureMultiplier;
        return this;
    }

    public ParticleSimulator setViscosityStrength(float viscosityStrength) {
        this.viscosityStrength = Math.max(0.0f, viscosityStrength);
        return this;
    }

    public ParticleSimulator setConcentration(Vector3 center, float concentration) {
        if (center != null) {
            this.center = center;
        }
        this.concentration = Math.max(0.0f, concentration);
        return this;
    }

    public ParticleSimulator setBounceDamping(float bounceDamping) {
        this.bounceDamping = Math.max(0.0f, bounceDamping);
        return this;
    }

    public ParticleCollider getCollider() {
        return collider;
    }

    public ParticleSimulator setColliderSize(float width, float height, float depth) {
        collider.setSize(width, height, depth);
        return this;
    }

    public ParticleSimulator setColliderSize(Vector3 size) {
        collider.setSize(size);
        return this;
    }

    public ParticleSimulator addColliderSize(float widthDelta, float heightDelta, float depthDelta) {
        collider.addSize(widthDelta, heightDelta, depthDelta);
        return this;
    }

    public ParticleSimulator addColliderUniformSize(float delta) {
        collider.addUniformSize(delta);
        return this;
    }

    public ParticleSimulator setColliderCenter(float x, float y, float z) {
        collider.setCenter(x, y, z);
        return this;
    }

    public ParticleSimulator setColliderCenter(Vector3 center) {
        collider.setCenter(center);
        return this;
    }

    public ParticleSimulator addColliderCenter(float xDelta, float yDelta, float zDelta) {
        collider.addCenter(xDelta, yDelta, zDelta);
        return this;
    }

    public void dispose() {
        externalForcesShader.dispose();
        spatialHashShader.dispose();
        densityShader.dispose();
        pressureShader.dispose();
        viscosityShader.dispose();
        updatePositionsShader.dispose();
        gpuSort.dispose();
        collider.dispose();
        colliderBuffer.dispose();
    }

    private void runExternalForces(ParticleBuffer buffer, float stepDeltaTime) {
        externalForcesShader.bind();
        buffer.getPositionBuffer().bindBase(0);
        buffer.getPredictedPositionBuffer().bindBase(1);
        buffer.getVelocityBuffer().bindBase(2);
        externalForcesShader.setInt("numParticles", buffer.getParticleCount());
        externalForcesShader.setFloat("deltaTime", stepDeltaTime);
        externalForcesShader.setVector3("gravity", gravity);
        externalForcesShader.setVector3("center", center);
        externalForcesShader.setFloat("concentration", concentration);
        ComputeHelper.dispatch(externalForcesShader, buffer.getParticleCount());
        ComputeHelper.memoryBarrier();
        externalForcesShader.unbind();
    }

    private void runSpatialHash(ParticleBuffer buffer) {
        spatialHashShader.bind();
        buffer.getPredictedPositionBuffer().bindBase(0);
        buffer.getSpatialIndexBuffer().bindBase(1);
        buffer.getSpatialOffsetBuffer().bindBase(2);
        spatialHashShader.setInt("numParticles", buffer.getParticleCount());
        spatialHashShader.setFloat("smoothingRadius", smoothingRadius);
        ComputeHelper.dispatch(spatialHashShader, buffer.getParticleCount());
        ComputeHelper.memoryBarrier();
        spatialHashShader.unbind();
    }

    private void runDensity(ParticleBuffer buffer) {
        densityShader.bind();
        bindNeighbourBuffers(densityShader, buffer);
        densityShader.setFloat("smoothingRadius", smoothingRadius);
        ComputeHelper.dispatch(densityShader, buffer.getParticleCount());
        ComputeHelper.memoryBarrier();
        densityShader.unbind();
    }

    private void runPressure(ParticleBuffer buffer, float stepDeltaTime) {
        pressureShader.bind();
        bindNeighbourBuffers(pressureShader, buffer);
        buffer.getVelocityBuffer().bindBase(4);
        pressureShader.setFloat("deltaTime", stepDeltaTime);
        pressureShader.setFloat("smoothingRadius", smoothingRadius);
        pressureShader.setFloat("targetDensity", targetDensity);
        pressureShader.setFloat("pressureMultiplier", pressureMultiplier);
        pressureShader.setFloat("nearPressureMultiplier", nearPressureMultiplier);
        ComputeHelper.dispatch(pressureShader, buffer.getParticleCount());
        ComputeHelper.memoryBarrier();
        pressureShader.unbind();
    }

    private void runViscosity(ParticleBuffer buffer, float stepDeltaTime) {
        viscosityShader.bind();
        bindNeighbourBuffers(viscosityShader, buffer);
        buffer.getVelocityBuffer().bindBase(4);
        viscosityShader.setFloat("deltaTime", stepDeltaTime);
        viscosityShader.setFloat("smoothingRadius", smoothingRadius);
        viscosityShader.setFloat("viscosityStrength", viscosityStrength);
        ComputeHelper.dispatch(viscosityShader, buffer.getParticleCount());
        ComputeHelper.memoryBarrier();
        viscosityShader.unbind();
    }

    private void runUpdatePositions(ParticleBuffer buffer, float stepDeltaTime) {
        updatePositionsShader.bind();
        updateColliderBuffer();
        buffer.getPositionBuffer().bindBase(0);
        buffer.getVelocityBuffer().bindBase(1);
        colliderBuffer.bindBase(2);
        updatePositionsShader.setInt("numParticles", buffer.getParticleCount());
        updatePositionsShader.setFloat("deltaTime", stepDeltaTime);
        updatePositionsShader.setFloat("bounceDamping", bounceDamping);
        ComputeHelper.dispatch(updatePositionsShader, buffer.getParticleCount());
        ComputeHelper.memoryBarrier();
        updatePositionsShader.unbind();
    }

    private void updateColliderBuffer() {
        colliderBuffer.setData(collider.toBufferData());
    }

    private void bindNeighbourBuffers(ComputeShader shader, ParticleBuffer buffer) {
        buffer.getPredictedPositionBuffer().bindBase(0);
        buffer.getDensityBuffer().bindBase(1);
        buffer.getSpatialIndexBuffer().bindBase(2);
        buffer.getSpatialOffsetBuffer().bindBase(3);
        shader.setInt("numParticles", buffer.getParticleCount());
    }
}
