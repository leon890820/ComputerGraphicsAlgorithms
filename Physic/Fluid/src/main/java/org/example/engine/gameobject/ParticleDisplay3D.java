package org.example.engine.gameobject;

import org.example.engine.component.MeshRenderer;
import org.example.engine.component.ParticleBuffer;
import org.example.engine.component.ParticleSimulator;
import org.example.engine.component.ParticleSpawn;
import org.example.engine.gl.ComputeBuffer;
import org.example.engine.material.Particle3DMaterial;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SphereGenerator;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;

public class ParticleDisplay3D extends GameObject {
    private final ParticleBuffer particleBuffer;
    private final ParticleSimulator simulator;
    private final Particle3DMaterial material;

    public ParticleDisplay3D() {
        this(new ParticleSpawn());
    }

    public ParticleDisplay3D(ParticleSpawn spawnComponent) {
        ParticleSpawn spawn = spawnComponent == null
                ? new ParticleSpawn()
                : spawnComponent;

        particleBuffer = new ParticleBuffer(spawn);
        simulator = new ParticleSimulator();

        material = new Particle3DMaterial(
                particleBuffer.getPositionBuffer(),
                particleBuffer.getVelocityBuffer()
        )
                .setScale(spawn.getParticleRadius())
                .setVelocityMax(1.0f);

        Mesh mesh = SphereGenerator.generateSphereMesh(spawn.getMeshResolution());
        setMesh(mesh);
        buildSubMeshRenderers(material);
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
    public void run(RenderContext ctx) {
        simulator.update(particleBuffer);

        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.renderInstanced(
                        createMaterialRenderData(ctx, mr),
                        material,
                        particleBuffer.getParticleCount()
                );
            }
        }

        simulator.getCollider().debugDraw(ctx);
    }

    @Override
    public void dispose(ResourceDisposalContext disposalContext) {
        super.dispose(disposalContext);
        simulator.dispose();
        particleBuffer.dispose();
    }
}
