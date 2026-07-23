package org.example.engine.component;

import org.example.engine.gameobject.GameObject;
import org.example.engine.material.Particle3DMaterial;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SphereGenerator;
import org.example.engine.render.RenderContext;

public class ParticleDisplay extends FluidDisplay {
    private final GameObject gameObject;
    private final FluidSimulation simulation;
    private final ParticleSpawn spawn;
    private final Particle3DMaterial material;

    public ParticleDisplay(GameObject gameObject, FluidSimulation simulation, ParticleSpawn spawnComponent) {
        super(FluidRenderMode.PARTICLES);
        this.gameObject = gameObject;
        this.simulation = simulation;
        spawn = spawnComponent == null ? new ParticleSpawn() : spawnComponent;
        material = new Particle3DMaterial(
                simulation.getPositionBuffer(),
                simulation.getVelocityBuffer()
        )
                .setScale(spawn.getParticleRadius())
                .setVelocityMax(1.0f)
                .setColour(0.0f, 1.0f, 0.74f)
                .setRimColour(0.45f, 1.0f, 0.82f);
    }

    @Override
    public void onAttach() {
        Mesh mesh = SphereGenerator.generateSphereMesh(spawn.getMeshResolution());
        gameObject.setMesh(mesh);
        gameObject.buildSubMeshRenderers(material);
        for (MeshRenderer renderer : gameObject.getMeshRenderers()) {
            if (renderer != null) {
                renderer.setEnabled(false);
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        MaterialRenderData renderData = createWorldSpaceRenderData(ctx);

        for (MeshRenderer renderer : gameObject.getMeshRenderers()) {
            if (renderer != null) {
                renderer.renderInstanced(
                        renderData,
                        material,
                        simulation.getParticleCount()
                );
            }
        }
    }

    private MaterialRenderData createWorldSpaceRenderData(RenderContext ctx) {
        MaterialRenderData data = new MaterialRenderData();
        data.modelMatrix = Matrix4.Identity();

        if (ctx != null && ctx.camera != null) {
            data.viewPosition = ctx.camera.transform.position;
            data.mvpMatrix = ctx.camera.Matrix();
        }

        return data;
    }
}
