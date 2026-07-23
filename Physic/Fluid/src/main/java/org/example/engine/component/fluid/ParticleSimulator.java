package org.example.engine.component;

import org.example.engine.gl.ComputeHelper;
import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.ComputeShader;
import org.example.engine.gl.Texture3D;
import org.example.engine.math.Vector3;

public class ParticleSimulator {
    private static final int DENSITY_VOLUME_SIZE = 128;

    private final ComputeShader externalForcesShader;
    private final ComputeShader spatialHashShader;
    private final ComputeShader pressureShader;
    private final ComputeShader densityShader;
    private final ComputeShader viscosityShader;
    private final ComputeShader updatePositionsShader;
    private final ComputeShader densityVolumeShader;
    private final ComputeShader densitySmoothShader;
    private final GPUSort gpuSort;
    private final ParticleCollider collider;
    private final Texture3D rawDensityVolumeTexture;
    private final Texture3D densityVolumeTexture;

    private Vector3 densityBoundsCenter = new Vector3(0.0f);
    private Vector3 densityBoundsSize = new Vector3(1.0f);
    private float deltaTime = 1.0f / 60.0f;
    private float timeScale = 1.0f;
    private int iterationsPerFrame = 2;
    private Vector3 gravity = new Vector3(0.0f, -9.8f, 0.0f);
    private float smoothingRadius = 0.2f;
    private float targetDensity = 15.0f;
    private float pressureMultiplier = 88.0f;
    private float nearPressureMultiplier = 2.25f;
    private float viscosityStrength = 0.001f;
    private Vector3 center = new Vector3(0.0f, 0.0f, 0.0f);
    private float concentration = 0.0f;
    private float containmentRadius = 1.0f;
    private float containmentStrength = 0.0f;
    private float maxSpeed = 8.0f;
    private float bounceDamping = 0.99f;
    private float floorY = 0.0f;
    private float floorFriction = 5.0f;
    private float floorFrictionDistance = 0.08f;

    public ParticleSimulator() {
        externalForcesShader = new ComputeShader("/shaders/particle/compute/particle_external_forces.comp");
        spatialHashShader = new ComputeShader("/shaders/particle/compute/particle_spatial_hash.comp");
        densityShader = new ComputeShader("/shaders/particle/compute/particle_calculate_densities.comp");
        pressureShader = new ComputeShader("/shaders/particle/compute/particle_pressure.comp");
        viscosityShader = new ComputeShader("/shaders/particle/compute/particle_viscosity.comp");
        updatePositionsShader = new ComputeShader("/shaders/particle/compute/particle_update.comp");
        densityVolumeShader = new ComputeShader("/shaders/particle/compute/particle_density_volume.comp");
        densitySmoothShader = new ComputeShader("/shaders/particle/compute/particle_density_smooth.comp");
        gpuSort = new GPUSort();
        collider = new ParticleCollider();
        rawDensityVolumeTexture = ComputeHelper.createVolumeTexture(DENSITY_VOLUME_SIZE);
        densityVolumeTexture = ComputeHelper.createVolumeTexture(DENSITY_VOLUME_SIZE);
    }

    public void update(ParticleBuffer buffer) {
        if (buffer == null || buffer.getParticleCount() <= 0) {
            return;
        }

        float stepDeltaTime = deltaTime / Math.max(1, iterationsPerFrame) * timeScale;

        for (int i = 0; i < iterationsPerFrame; i++) {
            runSimulationStep(buffer, stepDeltaTime);
        }

        runDensityVolume(buffer);
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

    public ParticleSimulator setContainment(float radius, float strength, float maxSpeed) {
        containmentRadius = Math.max(0.0001f, radius);
        containmentStrength = Math.max(0.0f, strength);
        this.maxSpeed = Math.max(0.0001f, maxSpeed);
        return this;
    }

    public ParticleSimulator setBounceDamping(float bounceDamping) {
        this.bounceDamping = Math.max(0.0f, bounceDamping);
        return this;
    }

    public ParticleSimulator setFloorY(float floorY) {
        this.floorY = floorY;
        return this;
    }

    public ParticleSimulator setFloorFriction(float floorFriction) {
        this.floorFriction = Math.max(0.0f, floorFriction);
        return this;
    }

    public ParticleSimulator setFloorFrictionDistance(float floorFrictionDistance) {
        this.floorFrictionDistance = Math.max(0.0001f, floorFrictionDistance);
        return this;
    }

    public ParticleSimulator setFloorFriction(float floorFriction, float floorFrictionDistance) {
        return setFloorFriction(floorFriction)
                .setFloorFrictionDistance(floorFrictionDistance);
    }

    public ParticleCollider getCollider() {
        return collider;
    }

    public Texture3D getDensityVolumeTexture() {
        return densityVolumeTexture;
    }

    public Vector3 getDensityBoundsCenter() {
        return densityBoundsCenter.copy();
    }

    public Vector3 getDensityBoundsSize() {
        return densityBoundsSize.copy();
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

    public ColliderGizmo.Mode toggleColliderGizmoMode() {
        return collider.toggleGizmoMode();
    }

    public void dispose() {
        externalForcesShader.dispose();
        spatialHashShader.dispose();
        densityShader.dispose();
        pressureShader.dispose();
        viscosityShader.dispose();
        updatePositionsShader.dispose();
        densityVolumeShader.dispose();
        densitySmoothShader.dispose();
        gpuSort.dispose();
        collider.dispose();
        rawDensityVolumeTexture.dispose();
        densityVolumeTexture.dispose();
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
        externalForcesShader.setFloat("containmentRadius", containmentRadius);
        externalForcesShader.setFloat("containmentStrength", containmentStrength);
        externalForcesShader.setFloat("maxSpeed", maxSpeed);
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
        buffer.getPositionBuffer().bindBase(0);
        buffer.getVelocityBuffer().bindBase(1);
        updatePositionsShader.setInt("numParticles", buffer.getParticleCount());
        updatePositionsShader.setFloat("deltaTime", stepDeltaTime);
        updatePositionsShader.setFloat("bounceDamping", bounceDamping);
        updatePositionsShader.setFloat("floorY", floorY);
        updatePositionsShader.setFloat("floorFriction", floorFriction);
        updatePositionsShader.setFloat("floorFrictionDistance", floorFrictionDistance);
        ComputeHelper.dispatch(updatePositionsShader, buffer.getParticleCount());
        ComputeHelper.memoryBarrier();
        updatePositionsShader.unbind();
    }

    private void runDensityVolume(ParticleBuffer buffer) {
        updateDensityBounds(buffer);

        densityVolumeShader.bind();
        buffer.getPredictedPositionBuffer().bindBase(0);
        buffer.getSpatialIndexBuffer().bindBase(1);
        buffer.getSpatialOffsetBuffer().bindBase(2);
        ComputeHelper.assignImage(densityVolumeShader, rawDensityVolumeTexture, "DensityVolume", 0);
        densityVolumeShader.setInt("numParticles", buffer.getParticleCount());
        densityVolumeShader.setFloat("smoothingRadius", smoothingRadius);
        densityVolumeShader.setVector3("boundsCenter", densityBoundsCenter);
        densityVolumeShader.setVector3("boundsSize", densityBoundsSize);
        ComputeHelper.dispatch(densityVolumeShader, densityVolumeTexture);
        ComputeHelper.memoryBarrier();
        densityVolumeShader.unbind();

        smoothDensityVolume();
    }

    private void smoothDensityVolume() {
        densitySmoothShader.bind();
        densitySmoothShader.setTexture("SourceDensityVolume", rawDensityVolumeTexture, 0);
        ComputeHelper.assignImage(densitySmoothShader, densityVolumeTexture, "SmoothedDensityVolume", 0);
        ComputeHelper.dispatch(densitySmoothShader, densityVolumeTexture);
        ComputeHelper.memoryBarrier();
        densitySmoothShader.unbind();
    }

    private void updateDensityBounds(ParticleBuffer buffer) {
        float[] positions = buffer.getPredictedPositionBuffer().getFloatData(buffer.getParticleCount() * 4);

        if (positions.length < 4) {
            densityBoundsCenter = new Vector3(0.0f);
            densityBoundsSize = new Vector3(1.0f);
            return;
        }

        float boundsClampRadius = Math.max(containmentRadius + smoothingRadius * 2.0f, smoothingRadius);
        float minAllowedX = center.x - boundsClampRadius;
        float minAllowedY = center.y - boundsClampRadius;
        float minAllowedZ = center.z - boundsClampRadius;
        float maxAllowedX = center.x + boundsClampRadius;
        float maxAllowedY = center.y + boundsClampRadius;
        float maxAllowedZ = center.z + boundsClampRadius;

        float firstX = clamp(positions[0], minAllowedX, maxAllowedX);
        float firstY = clamp(positions[1], minAllowedY, maxAllowedY);
        float firstZ = clamp(positions[2], minAllowedZ, maxAllowedZ);
        float minX = firstX;
        float minY = firstY;
        float minZ = firstZ;
        float maxX = firstX;
        float maxY = firstY;
        float maxZ = firstZ;

        for (int i = 4; i + 2 < positions.length; i += 4) {
            float x = clamp(positions[i], minAllowedX, maxAllowedX);
            float y = clamp(positions[i + 1], minAllowedY, maxAllowedY);
            float z = clamp(positions[i + 2], minAllowedZ, maxAllowedZ);

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        float margin = Math.max(smoothingRadius, 0.0001f);
        minX -= margin;
        minY -= margin;
        minZ -= margin;
        maxX += margin;
        maxY += margin;
        maxZ += margin;

        float width = Math.max(0.0001f, maxX - minX);
        float height = Math.max(0.0001f, maxY - minY);
        float depth = Math.max(0.0001f, maxZ - minZ);

        densityBoundsCenter = new Vector3(
                (minX + maxX) * 0.5f,
                (minY + maxY) * 0.5f,
                (minZ + maxZ) * 0.5f
        );
        densityBoundsSize = new Vector3(width, height, depth);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private void bindNeighbourBuffers(ComputeShader shader, ParticleBuffer buffer) {
        buffer.getPredictedPositionBuffer().bindBase(0);
        buffer.getDensityBuffer().bindBase(1);
        buffer.getSpatialIndexBuffer().bindBase(2);
        buffer.getSpatialOffsetBuffer().bindBase(3);
        shader.setInt("numParticles", buffer.getParticleCount());
    }
}
