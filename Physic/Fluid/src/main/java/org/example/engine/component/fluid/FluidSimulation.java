package org.example.engine.component;

import org.example.engine.gl.ComputeBuffer;

public class FluidSimulation extends Component {
    private final ParticleBuffer particleBuffer;
    private final ParticleSimulator simulator;

    public FluidSimulation() {
        this(new ParticleSpawn());
    }

    public FluidSimulation(ParticleSpawn spawnComponent) {
        ParticleSpawn spawn = spawnComponent == null ? new ParticleSpawn() : spawnComponent;
        particleBuffer = new ParticleBuffer(spawn);
        simulator = new ParticleSimulator();
    }

    @Override
    public void update(float deltaTime) {
        simulator.update(particleBuffer);
    }

    public ComputeBuffer getPositionBuffer() {
        return particleBuffer.getPositionBuffer();
    }

    public ComputeBuffer getVelocityBuffer() {
        return particleBuffer.getVelocityBuffer();
    }

    public int getParticleCount() {
        return particleBuffer.getParticleCount();
    }

    public ParticleBuffer getParticleBuffer() {
        return particleBuffer;
    }

    public ParticleSimulator getSimulator() {
        return simulator;
    }

    @Override
    public void dispose() {
        simulator.dispose();
        particleBuffer.dispose();
    }
}
