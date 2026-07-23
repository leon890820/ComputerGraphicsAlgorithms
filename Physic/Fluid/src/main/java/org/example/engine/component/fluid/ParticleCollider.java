package org.example.engine.component;

import org.example.engine.gameobject.GameObject;
import org.example.engine.material.DebugLineMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Transform;

public class ParticleCollider {
    public static final int COLLIDER_STRIDE = 32 * Float.BYTES;

    private final Transform transform = new Transform();
    private final GameObject debugBox;
    private final DebugLineMaterial debugMaterial;
    private final ColliderGizmo gizmo;
    private boolean debugDrawEnabled = true;

    public ParticleCollider() {
        transform.setScale(new Vector3(3.0f, 2.0f, 3.0f));
        debugMaterial = new DebugLineMaterial();
        debugBox = new GameObject() {
        };
        debugBox.setMesh(createDebugBoxMesh());
        debugBox.setTransform(transform);
        debugBox.buildSubMeshRenderers(debugMaterial);
        gizmo = new ColliderGizmo();
    }

    public Transform getTransform() {
        return transform;
    }

    public ParticleCollider setSize(float width, float height, float depth) {
        transform.setScale(
                Math.max(0.0001f, width),
                Math.max(0.0001f, height),
                Math.max(0.0001f, depth)
        );
        return this;
    }

    public ParticleCollider setSize(Vector3 size) {
        if (size != null) {
            setSize(size.x, size.y, size.z);
        }
        return this;
    }

    public ParticleCollider addSize(float widthDelta, float heightDelta, float depthDelta) {
        Vector3 size = getSize();
        return setSize(size.x + widthDelta, size.y + heightDelta, size.z + depthDelta);
    }

    public ParticleCollider addUniformSize(float delta) {
        return addSize(delta, delta, delta);
    }

    public Vector3 getSize() {
        return transform.scale.copy();
    }

    public ParticleCollider setCenter(float x, float y, float z) {
        transform.setPosition(x, y, z);
        return this;
    }

    public ParticleCollider setCenter(Vector3 center) {
        if (center != null) {
            transform.setPosition(center);
        }
        return this;
    }

    public ParticleCollider addCenter(float xDelta, float yDelta, float zDelta) {
        Vector3 center = getCenter();
        return setCenter(center.x + xDelta, center.y + yDelta, center.z + zDelta);
    }

    public Vector3 getCenter() {
        return transform.position.copy();
    }

    public float[] toBufferData() {
        float[] data = new float[32];
        putMatrix(data, 0, transform.localToWorld());
        putMatrix(data, 16, transform.worldToLocal());
        return data;
    }

    public ParticleCollider setDebugDrawEnabled(boolean debugDrawEnabled) {
        this.debugDrawEnabled = debugDrawEnabled;
        if (!debugDrawEnabled) {
            gizmo.resetInteraction();
        }
        return this;
    }

    public ParticleCollider setDebugColour(float r, float g, float b) {
        debugMaterial.setColour(r, g, b);
        return this;
    }

    public ColliderGizmo.Mode toggleGizmoMode() {
        return gizmo.toggleMode();
    }

    public ColliderGizmo.Mode getGizmoMode() {
        return gizmo.getMode();
    }

    public void debugDraw(RenderContext ctx) {
        if (!debugDrawEnabled || ctx == null) {
            return;
        }

        for (MeshRenderer renderer : debugBox.getMeshRenderers()) {
            renderer.debugRender(ctx);
        }
    }

    public void updateGizmo(
            Transform moveTarget,
            Camera camera,
            int screenWidth,
            int screenHeight,
            double mouseX,
            double mouseY,
            boolean leftMouseDown,
            boolean leftMousePressed,
            boolean leftMouseReleased
    ) {
        gizmo.update(
                this,
                moveTarget,
                camera,
                screenWidth,
                screenHeight,
                mouseX,
                mouseY,
                leftMouseDown,
                leftMousePressed,
                leftMouseReleased
        );
    }

    public void updateGizmo(
            Camera camera,
            int screenWidth,
            int screenHeight,
            double mouseX,
            double mouseY,
            boolean leftMouseDown,
            boolean leftMousePressed,
            boolean leftMouseReleased
    ) {
        updateGizmo(
                null,
                camera,
                screenWidth,
                screenHeight,
                mouseX,
                mouseY,
                leftMouseDown,
                leftMousePressed,
                leftMouseReleased
        );
    }

    public void drawGizmo(Transform moveTarget, RenderContext ctx) {
        if (!debugDrawEnabled) {
            return;
        }

        gizmo.draw(this, moveTarget, ctx);
    }

    public void dispose() {
        ResourceDisposalContext disposalContext = new ResourceDisposalContext();
        debugBox.dispose(disposalContext);
        disposalContext.disposeAll();
        gizmo.dispose();
    }

    private void putMatrix(float[] data, int offset, Matrix4 matrix) {
        System.arraycopy(matrix.m, 0, data, offset, 16);
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

        SubMesh subMesh = new SubMesh("particle_collider_debug");
        subMesh.setGeometry(positions, null, null, indices, null, null, -1);

        Mesh mesh = new Mesh();
        mesh.addSubMesh(subMesh);
        return mesh;
    }
}
