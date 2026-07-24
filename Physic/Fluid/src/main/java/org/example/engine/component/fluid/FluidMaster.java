package org.example.engine.component.fluid;

import org.example.engine.component.core.Component;
import org.example.engine.gl.ComputeBuffer;
import org.example.engine.math.Vector3;

public class FluidMaster extends Component {
    private static final Vector3 DEFAULT_ATTRACTION_CENTER_OFFSET = new Vector3(0.0f,0.3f,0.0f);
    private static final float DEFAULT_COLLIDER_MARGIN = 0.2f;

    private final ParticleSpawn spawn;
    private FluidSimulation simulation;
    private FluidRenderer renderer;
    private Vector3 attractionCenter = new Vector3(0.0f);
    private Vector3 attractionCenterOffset = DEFAULT_ATTRACTION_CENTER_OFFSET;
    private float attractionStrength = 0.0f;
    private float containmentRadius = 1.0f;
    private float containmentStrength = 0.0f;
    private float maxSpeed = 8.0f;
    private boolean attractionFollowsTransform = true;
    private boolean colliderFollowsSpawnBounds = false;
    private Vector3 colliderLocalCenter = new Vector3(0.0f, 0.5f, 0.0f);

    public FluidMaster() {
        this(new ParticleSpawn());
    }

    public FluidMaster(ParticleSpawn spawnComponent) {
        spawn = spawnComponent == null ? new ParticleSpawn() : spawnComponent;
    }

    @Override
    protected void onAttach() {
        simulation = gameObject.addComponent(new FluidSimulation(spawn));
        renderer = gameObject.addComponent(new FluidRenderer(simulation, spawn));
        applyColliderBounds();
        applyAttraction();
    }

    @Override
    public void update(float deltaTime) {
        applyColliderFollow();
        applyAttraction();
    }

    public FluidSimulation getSimulation() {
        return simulation;
    }

    public FluidRenderer getRenderer() {
        return renderer;
    }

    public ComputeBuffer getPositionBuffer() {
        return simulation.getPositionBuffer();
    }

    public ComputeBuffer getVelocityBuffer() {
        return simulation.getVelocityBuffer();
    }

    public int getParticleCount() {
        return simulation.getParticleCount();
    }

    public ParticleBuffer getParticleBuffer() {
        return simulation.getParticleBuffer();
    }

    public ParticleSimulator getSimulator() {
        return simulation.getSimulator();
    }

    public FluidRenderMode getFluidRenderMode() {
        return renderer.getFluidRenderMode();
    }

    public FluidMaster setFluidRenderMode(FluidRenderMode mode) {
        renderer.setFluidRenderMode(mode);
        return this;
    }

    public FluidRenderMode toggleFluidRenderMode() {
        return renderer.toggleFluidRenderMode();
    }

    public boolean toggleDebugDrawEnabled() {
        return renderer.toggleDebugDrawEnabled();
    }

    public boolean isDebugDrawEnabled() {
        return renderer.isDebugDrawEnabled();
    }

    public FluidMaster setCenterAttraction(float strength) {
        attractionStrength = Math.max(0.0f, strength);
        applyAttraction();
        return this;
    }

    public FluidMaster setCenterAttractionOffset(Vector3 offset) {
        if (offset != null) {
            attractionCenterOffset = offset;
        }
        applyAttraction();
        return this;
    }

    public FluidMaster setCenterAttraction(Vector3 center, float strength) {
        attractionFollowsTransform = false;
        if (center != null) {
            attractionCenter = center;
        }
        attractionStrength = Math.max(0.0f, strength);
        applyAttraction();
        return this;
    }

    public FluidMaster setAttractionFollowsTransform(boolean followsTransform) {
        attractionFollowsTransform = followsTransform;
        applyAttraction();
        return this;
    }

    public FluidMaster setContainment(float radius, float strength, float maxSpeed) {
        containmentRadius = Math.max(0.0001f, radius);
        containmentStrength = Math.max(0.0f, strength);
        this.maxSpeed = Math.max(0.0001f, maxSpeed);
        applyAttraction();
        return this;
    }

    public FluidMaster setColliderFollowsSpawnBounds(boolean followsSpawnBounds) {
        colliderFollowsSpawnBounds = followsSpawnBounds;
        applyColliderBounds();
        return this;
    }

    public FluidMaster setColliderLocalCenter(Vector3 localCenter) {
        if (localCenter != null) {
            colliderLocalCenter = localCenter;
        }
        applyColliderFollow();
        return this;
    }

    private void applyAttraction() {
        if (simulation == null) {
            return;
        }

        Vector3 center = attractionCenter;
        if (attractionFollowsTransform && gameObject != null) {
            center = gameObject.transform.position.add(attractionCenterOffset);
        }

        simulation.getSimulator().setConcentration(center, attractionStrength);
        simulation.getSimulator().setContainment(containmentRadius, containmentStrength, maxSpeed);
    }

    private void applyColliderBounds() {
        if (!colliderFollowsSpawnBounds || simulation == null || gameObject == null) {
            return;
        }

        float spawnSpan = spawn.getSpawnSpan();
        float width = Math.max(1.0f, spawnSpan + DEFAULT_COLLIDER_MARGIN);
        float height = Math.max(1.0f, spawnSpan + DEFAULT_COLLIDER_MARGIN);
        float depth = Math.max(1.0f, spawnSpan + DEFAULT_COLLIDER_MARGIN);
        Vector3 base = gameObject.transform.position;
        colliderLocalCenter = new Vector3(0.0f, height * 0.5f, 0.0f);
        Vector3 center = base.add(colliderLocalCenter);

        simulation.getSimulator().setColliderSize(width, height, depth);
        simulation.getSimulator().setColliderCenter(center);
    }

    private void applyColliderFollow() {
        if (!colliderFollowsSpawnBounds || simulation == null || gameObject == null) {
            return;
        }

        Vector3 colliderSize = simulation.getSimulator().getCollider().getSize();
        colliderLocalCenter = new Vector3(colliderLocalCenter.x, colliderSize.y * 0.5f, colliderLocalCenter.z);
        simulation.getSimulator().setColliderCenter(gameObject.transform.position.add(colliderLocalCenter));
    }
}
