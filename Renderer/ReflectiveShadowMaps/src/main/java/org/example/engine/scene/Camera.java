package org.example.engine.scene;

import org.example.engine.gameobject.GameObject;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.math.Vector4;
import org.example.engine.gameobject.Quad;

public class Camera extends Quad {

    Matrix4 projection = new Matrix4();
    Matrix4 worldView = new Matrix4();

    float wid;
    float hei;
    float near;
    float far;

    private Matrix4 cachedVP = new Matrix4();

    private boolean projectionDirty = true;
    private boolean viewDirty = true;
    private boolean vpDirty = true;

    public static float GH_FOV = 45.0f;

    public Camera() {
        super(null);
        wid = 256.0f;
        hei = 256.0f;
        near = 0.1f;
        far = 10000.0f;

        worldView.makeIdentity();
        projection.makeIdentity();
        cachedVP.makeIdentity();

        projectionDirty = true;
        viewDirty = true;
        vpDirty = true;
    }

    private void markProjectionDirty() {
        projectionDirty = true;
        vpDirty = true;
    }

    private void markViewDirty() {
        viewDirty = true;
        vpDirty = true;
    }

    private float clampPitch(float pitch) {
        float limit = (float) Math.toRadians(89.0f);
        return Math.max(-limit, Math.min(limit, pitch));
    }

    private void rebuildProjectionIfNeeded() {
        if (!projectionDirty) return;

        float fovRad = (float) Math.toRadians(GH_FOV);
        float f = 1.0f / (float) Math.tan(fovRad / 2.0f);
        float aspect = wid / hei;

        projection.makeZero();

        projection.set(0, 0, f / aspect);
        projection.set(1, 1, f);
        projection.set(2, 2, (far + near) / (near - far));
        projection.set(2, 3, (2.0f * far * near) / (near - far));
        projection.set(3, 2, -1.0f);
        projection.set(3, 3, 0.0f);

        projectionDirty = false;
    }

    private void rebuildViewIfNeeded() {
        if (!viewDirty) return;

        Vector3 pos = transform.position;
        Vector3 rot = transform.eular;

        // View = Rx(-pitch) * Ry(-yaw) * T(-pos)
        worldView =
                Matrix4.RotX(-rot.x())
                        .mult(Matrix4.RotY(-rot.y()))
                        .mult(Matrix4.Trans(pos.mult(-1.0f)));

        viewDirty = false;
    }

    private void rebuildVPIfNeeded() {
        if (!vpDirty) return;

        rebuildProjectionIfNeeded();
        rebuildViewIfNeeded();

        cachedVP = projection.mult(worldView);
        vpDirty = false;
    }

    public Matrix4 inverseProjection() {
        rebuildProjectionIfNeeded();

        Matrix4 invProjection = Matrix4.Zero();

        float a = projection.get(0, 0);
        float b = projection.get(1, 1);
        float c = projection.get(2, 2);
        float d = projection.get(2, 3);
        float e = projection.get(3, 2);

        invProjection.set(0, 0, 1.0f / a);
        invProjection.set(1, 1, 1.0f / b);
        invProjection.set(2, 3, 1.0f / e);
        invProjection.set(3, 2, 1.0f / d);
        invProjection.set(3, 3, -c / (d * e));

        return invProjection;
    }

    public Matrix4 Matrix() {
        rebuildVPIfNeeded();
        return cachedVP;
    }

    public Matrix4 getProjectionMatrix() {
        rebuildProjectionIfNeeded();
        return projection;
    }

    public Matrix4 getViewMatrix() {
        rebuildViewIfNeeded();
        return worldView;
    }

    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        projection.makeZero();

        projection.set(0, 0, 2.0f / (right - left));
        projection.set(1, 1, 2.0f / (top - bottom));
        projection.set(2, 2, -2.0f / (far - near));

        projection.set(0, 3, -(right + left) / (right - left));
        projection.set(1, 3, -(top + bottom) / (top - bottom));
        projection.set(2, 3, -(far + near) / (far - near));

        projection.set(3, 3, 1.0f);

        projectionDirty = false;
        vpDirty = true;
    }

    public void setSize(float w, float h, float n, float f) {
        wid = w;
        hei = h;
        near = n;
        far = f;
        markProjectionDirty();
    }

    public void setPositionOrientation(Vector3 pos, float rotX, float rotY) {
        transform.setPosition(pos);
        transform.setEular(clampPitch(rotX), rotY, 0.0f);
        markViewDirty();
    }

    public void setPositionOrientation(Vector3 pos, Vector3 la) {
        transform.setPosition(pos);

        Vector3 f = la.sub(pos).unit_vector();

        float rotX = (float) Math.asin(f.y());
        float rotY = (float) Math.atan2(-f.x(), -f.z());

        transform.setEular(clampPitch(rotX), rotY, 0.0f);
        markViewDirty();
    }

    public void update() {
        if (transform.isDirty()) {
            markViewDirty();
        }

        rebuildViewIfNeeded();
    }

    public void clipOblique(Vector3 pos, Vector3 normal) {
        rebuildProjectionIfNeeded();
        rebuildViewIfNeeded();

        Vector3 cpos = worldView.mult(new Vector4(pos, 1.0f)).xyz();
        Vector3 cnormal = worldView.mult(new Vector4(normal, 0.0f)).xyz();

        Vector4 cplane = new Vector4(
                cnormal.x(),
                cnormal.y(),
                cnormal.z(),
                Vector3.dot(cpos.mult(-1.0f), cnormal)
        );

        Vector4 q = projection.Inverse().mult(new Vector4(
                (cplane.x < 0.0f ? 1.0f : -1.0f),
                (cplane.y < 0.0f ? 1.0f : -1.0f),
                1.0f,
                1.0f
        ));

        Vector4 c = cplane.mult(2.0f / cplane.dot(q));

        projection.set(2, 0, c.x - projection.get(3, 0));
        projection.set(2, 1, c.y - projection.get(3, 1));
        projection.set(2, 2, c.z - projection.get(3, 2));
        projection.set(2, 3, c.w - projection.get(3, 3));

        vpDirty = true;
    }
}