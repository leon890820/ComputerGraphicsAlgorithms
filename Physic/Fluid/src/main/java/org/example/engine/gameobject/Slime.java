package org.example.engine.gameobject;

import org.example.engine.component.FluidMaster;
import org.example.engine.component.FluidRenderMode;
import org.example.engine.component.ParticleBuffer;
import org.example.engine.component.ParticleSimulator;
import org.example.engine.component.ParticleSpawn;
import org.example.engine.gl.ComputeBuffer;
import org.example.engine.math.Vector3;

public class Slime extends GameObject {
    private static final float DEFAULT_CENTER_ATTRACTION = 30.0f;
    private static final float DEFAULT_CONTAINMENT_RADIUS = 1.0f;
    private static final float DEFAULT_CONTAINMENT_STRENGTH = 75.0f;
    private static final float DEFAULT_MAX_SPEED = 3.5f;
    private static final float DEFAULT_TARGET_DENSITY = 18.0f;
    private static final float DEFAULT_PRESSURE_MULTIPLIER = 70.0f;
    private static final float DEFAULT_NEAR_PRESSURE_MULTIPLIER = 4.5f;
    private static final float DEFAULT_VISCOSITY_STRENGTH = 0.018f;
    private static final float DEFAULT_BOUNCE_DAMPING = 0.35f;
    private static final float DEFAULT_FLOOR_FRICTION = 8.0f;
    private static final float DEFAULT_FLOOR_FRICTION_DISTANCE = 0.12f;

    private final FluidMaster fluid;

    public Slime() {
        this(new ParticleSpawn());
    }

    public Slime(ParticleSpawn spawnComponent) {
        fluid = addComponent(new FluidMaster(spawnComponent)
                .setCenterAttraction(DEFAULT_CENTER_ATTRACTION)
                .setContainment(DEFAULT_CONTAINMENT_RADIUS, DEFAULT_CONTAINMENT_STRENGTH, DEFAULT_MAX_SPEED)
                .setColliderFollowsSpawnBounds(true)
                .setAttractionFollowsTransform(true));
        fluid.getSimulator()
                .setPressure(
                        DEFAULT_TARGET_DENSITY,
                        DEFAULT_PRESSURE_MULTIPLIER,
                        DEFAULT_NEAR_PRESSURE_MULTIPLIER
                )
                .setViscosityStrength(DEFAULT_VISCOSITY_STRENGTH)
                .setBounceDamping(DEFAULT_BOUNCE_DAMPING)
                .setFloorFriction(DEFAULT_FLOOR_FRICTION, DEFAULT_FLOOR_FRICTION_DISTANCE);
    }

    public FluidMaster getFluid() {
        return fluid;
    }

    public Slime setCenterAttraction(float strength) {
        fluid.setCenterAttraction(strength);
        return this;
    }

    public Slime setCenterAttraction(Vector3 center, float strength) {
        fluid.setCenterAttraction(center, strength);
        return this;
    }

    public Slime setContainment(float radius, float strength, float maxSpeed) {
        fluid.setContainment(radius, strength, maxSpeed);
        return this;
    }

    public ComputeBuffer getPositionBuffer() {
        return fluid.getPositionBuffer();
    }

    public ComputeBuffer getVelocityBuffer() {
        return fluid.getVelocityBuffer();
    }

    public int getParticleCount() {
        return fluid.getParticleCount();
    }

    public ParticleBuffer getParticleBuffer() {
        return fluid.getParticleBuffer();
    }

    public ParticleSimulator getSimulator() {
        return fluid.getSimulator();
    }

    public FluidRenderMode getFluidRenderMode() {
        return fluid.getFluidRenderMode();
    }

    public Slime setFluidRenderMode(FluidRenderMode mode) {
        fluid.setFluidRenderMode(mode);
        return this;
    }

    public FluidRenderMode toggleFluidRenderMode() {
        return fluid.toggleFluidRenderMode();
    }

    public boolean toggleDebugDrawEnabled() {
        return fluid.toggleDebugDrawEnabled();
    }
}
