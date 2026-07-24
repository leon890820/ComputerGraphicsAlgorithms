package org.example.engine.component.fluid;

import org.example.engine.component.render.MeshRenderer;
import org.example.engine.gameobject.GameObject;
import org.example.engine.material.DebugLineMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;
import org.example.engine.scene.Transform;

public class SlimeAabbGizmo {
    private final ParticleSimulator simulator;
    private final Transform transform = new Transform();
    private final GameObject debugBox;
    private final DebugLineMaterial material;
    private boolean debugDrawEnabled = true;

    public SlimeAabbGizmo(ParticleSimulator simulator) {
        this.simulator = simulator;
        material = new DebugLineMaterial().setColour(0.0f, 0.85f, 1.0f);
        debugBox = new GameObject() {
        };
        debugBox.setMesh(createDebugBoxMesh());
        debugBox.setTransform(transform);
        debugBox.buildSubMeshRenderers(material);
    }

    public SlimeAabbGizmo setDebugDrawEnabled(boolean debugDrawEnabled) {
        this.debugDrawEnabled = debugDrawEnabled;
        return this;
    }

    public SlimeAabbGizmo setDebugColour(float r, float g, float b) {
        material.setColour(r, g, b);
        return this;
    }

    public void debugDraw(RenderContext ctx) {
        if (!debugDrawEnabled || ctx == null || simulator == null) {
            return;
        }

        Vector3 center = simulator.getDensityBoundsCenter();
        Vector3 size = simulator.getDensityBoundsSize();
        transform.setPosition(center);
        transform.setScale(size);

        for (MeshRenderer renderer : debugBox.getMeshRenderers()) {
            renderer.debugRender(ctx);
        }
    }

    public void dispose() {
        ResourceDisposalContext disposalContext = new ResourceDisposalContext();
        debugBox.dispose(disposalContext);
        disposalContext.disposeAll();
    }

    private Mesh createDebugBoxMesh() {
        float[] positions = {
                -0.5f, -0.5f, -0.5f,
                 0.5f, -0.5f, -0.5f,
                 0.5f,  0.5f, -0.5f,
                -0.5f,  0.5f, -0.5f,
                -0.5f, -0.5f,  0.5f,
                 0.5f, -0.5f,  0.5f,
                 0.5f,  0.5f,  0.5f,
                -0.5f,  0.5f,  0.5f
        };

        int[] indices = {
                0, 1, 1, 2, 2, 3, 3, 0,
                4, 5, 5, 6, 6, 7, 7, 4,
                0, 4, 1, 5, 2, 6, 3, 7
        };

        SubMesh subMesh = new SubMesh("slime_aabb_debug");
        subMesh.setGeometry(positions, null, null, indices, null, null, -1);

        Mesh mesh = new Mesh();
        mesh.addSubMesh(subMesh);
        return mesh;
    }
}
