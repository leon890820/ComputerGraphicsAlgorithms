package org.example.engine.component;

import org.example.engine.gameobject.GameObject;
import org.example.engine.material.DebugLineMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.math.Vector4;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Transform;

public class ColliderGizmo {
    private static final float AXIS_LENGTH = 0.8f;
    private static final float PICK_DISTANCE_PIXELS = 16.0f;
    private static final float DRAG_WORLD_PER_PIXEL = 0.006f;

    private final GameObject xAxis;
    private final GameObject yAxis;
    private final GameObject zAxis;

    private final DebugLineMaterial xMaterial;
    private final DebugLineMaterial yMaterial;
    private final DebugLineMaterial zMaterial;

    private Axis activeAxis = Axis.NONE;
    private Mode mode = Mode.MOVE;
    private double lastMouseX;
    private double lastMouseY;

    private enum Axis {
        NONE,
        X,
        Y,
        Z
    }

    public enum Mode {
        MOVE,
        SCALE
    }

    public ColliderGizmo() {
        xMaterial = new DebugLineMaterial().setColour(1.0f, 0.18f, 0.12f);
        yMaterial = new DebugLineMaterial().setColour(0.45f, 1.0f, 0.18f);
        zMaterial = new DebugLineMaterial().setColour(0.18f, 0.42f, 1.0f);

        xAxis = createAxisObject(Axis.X, xMaterial);
        yAxis = createAxisObject(Axis.Y, yMaterial);
        zAxis = createAxisObject(Axis.Z, zMaterial);
    }

    public void update(
            ParticleCollider collider,
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
        if (collider == null || camera == null) {
            return;
        }

        Vector3 gizmoCenter = getGizmoCenter(collider, moveTarget);

        if (leftMousePressed) {
            activeAxis = pickAxis(gizmoCenter, camera, screenWidth, screenHeight, mouseX, mouseY);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        if (leftMouseReleased) {
            activeAxis = Axis.NONE;
        }

        if (!leftMouseDown || activeAxis == Axis.NONE) {
            return;
        }

        Vector3 screenDirection = screenAxisDirection(
                gizmoCenter,
                axisVector(activeAxis),
                camera,
                screenWidth,
                screenHeight
        );

        double dx = mouseX - lastMouseX;
        double dy = mouseY - lastMouseY;
        float projectedDelta = (float) (dx * screenDirection.x + dy * screenDirection.y);
        Vector3 axisDelta = axisVector(activeAxis).mult(projectedDelta * DRAG_WORLD_PER_PIXEL);

        if (mode == Mode.MOVE) {
            if (moveTarget != null) {
                moveTarget.setPosition(moveTarget.position.add(axisDelta));
            } else {
                collider.addCenter(axisDelta.x, axisDelta.y, axisDelta.z);
            }
        } else {
            collider.addSize(axisDelta.x, axisDelta.y, axisDelta.z);
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.MOVE : mode;
        activeAxis = Axis.NONE;
    }

    public Mode toggleMode() {
        setMode(mode == Mode.MOVE ? Mode.SCALE : Mode.MOVE);
        return mode;
    }

    public void resetInteraction() {
        activeAxis = Axis.NONE;
    }

    public void draw(ParticleCollider collider, RenderContext ctx) {
        draw(collider, null, ctx);
    }

    public void draw(ParticleCollider collider, Transform moveTarget, RenderContext ctx) {
        if (collider == null || ctx == null) {
            return;
        }

        Vector3 center = getGizmoCenter(collider, moveTarget);
        updateAxisTransform(xAxis, center);
        updateAxisTransform(yAxis, center);
        updateAxisTransform(zAxis, center);

        for (MeshRenderer renderer : xAxis.getMeshRenderers()) {
            renderer.debugRender(ctx);
        }

        for (MeshRenderer renderer : yAxis.getMeshRenderers()) {
            renderer.debugRender(ctx);
        }

        for (MeshRenderer renderer : zAxis.getMeshRenderers()) {
            renderer.debugRender(ctx);
        }
    }

    public void dispose() {
        ResourceDisposalContext disposalContext = new ResourceDisposalContext();
        xAxis.dispose(disposalContext);
        yAxis.dispose(disposalContext);
        zAxis.dispose(disposalContext);
        disposalContext.disposeAll();
    }

    private Axis pickAxis(
            Vector3 center,
            Camera camera,
            int screenWidth,
            int screenHeight,
            double mouseX,
            double mouseY
    ) {
        float xDistance = distanceToScreenAxis(center, Axis.X, camera, screenWidth, screenHeight, mouseX, mouseY);
        float yDistance = distanceToScreenAxis(center, Axis.Y, camera, screenWidth, screenHeight, mouseX, mouseY);
        float zDistance = distanceToScreenAxis(center, Axis.Z, camera, screenWidth, screenHeight, mouseX, mouseY);

        Axis bestAxis = Axis.NONE;
        float bestDistance = PICK_DISTANCE_PIXELS;

        if (xDistance < bestDistance) {
            bestAxis = Axis.X;
            bestDistance = xDistance;
        }

        if (yDistance < bestDistance) {
            bestAxis = Axis.Y;
            bestDistance = yDistance;
        }

        if (zDistance < bestDistance) {
            bestAxis = Axis.Z;
        }

        return bestAxis;
    }

    private float distanceToScreenAxis(
            Vector3 center,
            Axis axis,
            Camera camera,
            int screenWidth,
            int screenHeight,
            double mouseX,
            double mouseY
    ) {
        Vector3 start = projectToScreen(center, camera, screenWidth, screenHeight);
        Vector3 end = projectToScreen(center.add(axisVector(axis).mult(AXIS_LENGTH)), camera, screenWidth, screenHeight);

        Vector3 segment = end.sub(start);
        float lenSq = Math.max(segment.x * segment.x + segment.y * segment.y, 0.0001f);
        float t = (float) (((mouseX - start.x) * segment.x + (mouseY - start.y) * segment.y) / lenSq);
        t = Math.max(0.0f, Math.min(1.0f, t));

        float closestX = start.x + segment.x * t;
        float closestY = start.y + segment.y * t;
        float dx = (float) mouseX - closestX;
        float dy = (float) mouseY - closestY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private Vector3 screenAxisDirection(
            Vector3 center,
            Vector3 axis,
            Camera camera,
            int screenWidth,
            int screenHeight
    ) {
        Vector3 start = projectToScreen(center, camera, screenWidth, screenHeight);
        Vector3 end = projectToScreen(center.add(axis.mult(AXIS_LENGTH)), camera, screenWidth, screenHeight);
        Vector3 direction = end.sub(start);
        float length = (float) Math.sqrt(direction.x * direction.x + direction.y * direction.y);

        if (length < 0.0001f) {
            return new Vector3(0.0f, 0.0f, 0.0f);
        }

        return new Vector3(direction.x / length, direction.y / length, 0.0f);
    }

    private Vector3 projectToScreen(Vector3 world, Camera camera, int screenWidth, int screenHeight) {
        Matrix4 vp = camera.Matrix();
        Vector4 clip = vp.mult(new Vector4(world, 1.0f));
        float invW = Math.abs(clip.w) < 0.0001f ? 1.0f : 1.0f / clip.w;
        float ndcX = clip.x * invW;
        float ndcY = clip.y * invW;

        return new Vector3(
                (ndcX * 0.5f + 0.5f) * screenWidth,
                (0.5f - ndcY * 0.5f) * screenHeight,
                clip.z * invW
        );
    }

    private Vector3 axisVector(Axis axis) {
        if (axis == Axis.X) {
            return Vector3.UnitX();
        }

        if (axis == Axis.Y) {
            return Vector3.UnitY();
        }

        if (axis == Axis.Z) {
            return Vector3.UnitZ();
        }

        return Vector3.Zero();
    }

    private void updateAxisTransform(GameObject axisObject, Vector3 center) {
        axisObject.setPosition(center);
        axisObject.setScale(AXIS_LENGTH, AXIS_LENGTH, AXIS_LENGTH);
    }

    private Vector3 getGizmoCenter(ParticleCollider collider, Transform moveTarget) {
        if (mode == Mode.MOVE && moveTarget != null) {
            return moveTarget.position;
        }

        return collider.getCenter();
    }

    private GameObject createAxisObject(Axis axis, DebugLineMaterial material) {
        GameObject object = new GameObject() {
        };
        object.setMesh(createAxisMesh(axis));
        object.buildSubMeshRenderers(material);
        return object;
    }

    private Mesh createAxisMesh(Axis axis) {
        Vector3 end = axisVector(axis);
        float[] positions = {
                0.0f, 0.0f, 0.0f,
                end.x, end.y, end.z
        };
        int[] indices = {0, 1};

        SubMesh subMesh = new SubMesh("collider_gizmo_axis");
        subMesh.setGeometry(positions, null, null, indices, null, null, -1);

        Mesh mesh = new Mesh();
        mesh.addSubMesh(subMesh);
        return mesh;
    }
}
