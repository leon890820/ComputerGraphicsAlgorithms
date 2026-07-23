package org.example.engine.gameobject;

import org.example.engine.component.FluidRenderMode;
import org.example.engine.component.FluidMaster;
import org.example.engine.component.ParticleBuffer;
import org.example.engine.component.ParticleSimulator;
import org.example.engine.component.ParticleSpawn;
import org.example.engine.gl.ComputeBuffer;

public class ParticleDisplay3D extends GameObject {
    private final FluidMaster fluid;

    public ParticleDisplay3D() {
        this(new ParticleSpawn());
    }

    public ParticleDisplay3D(ParticleSpawn spawnComponent) {
        fluid = addComponent(new FluidMaster(spawnComponent));
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

    public ParticleDisplay3D setFluidRenderMode(FluidRenderMode mode) {
        fluid.setFluidRenderMode(mode);
        return this;
    }

    public FluidRenderMode toggleFluidRenderMode() {
        return fluid.toggleFluidRenderMode();
    }
}
