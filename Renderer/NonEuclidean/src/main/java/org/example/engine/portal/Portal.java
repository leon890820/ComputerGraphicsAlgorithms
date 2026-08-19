package org.example.engine.portal;

import org.example.engine.gameobject.MeshObject;
import org.example.engine.gl.Texture;
import org.example.engine.component.render.MeshRenderer;
import org.example.engine.material.Material;
import org.example.engine.material.PortalMaterial;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;

public class Portal extends MeshObject {
    public final Warp front;
    public final Warp back;

    public Portal() {
        this(defaultMaterial());
    }

    public Portal(Material material) {
        super("/meshes/double_quad", material);
        front = new Warp(this);
        back = new Warp(this);
    }

    public Portal setRenderTexture(Texture texture) {
        for (MeshRenderer renderer : getMeshRenderers()) {
            if (renderer == null || !(renderer.getMaterial() instanceof PortalMaterial)) {
                continue;
            }

            ((PortalMaterial) renderer.getMaterial()).setRenderTexture(texture);
        }
        return this;
    }

    public Vector3 getBump(Vector3 point) {
        Vector3 normal = forward();
        return normal.mult(Vector3.dot(point.sub(transform.position), normal) > 0.0f ? 1.0f : -1.0f);
    }

    public Warp intersects(Vector3 from, Vector3 to, Vector3 bump) {
        Vector3 normal = forward();
        Vector3 portalPoint = transform.position.add(bump);
        float fromDistance = Vector3.dot(normal, from.sub(portalPoint));
        float toDistance = Vector3.dot(normal, to.sub(portalPoint));

        if (fromDistance * toDistance > 0.0f) {
            return null;
        }

        Matrix4 localToWorld = localToWorld();
        float distanceDelta = fromDistance - toDistance;
        if (Math.abs(distanceDelta) < 1e-6f) {
            return null;
        }

        Vector3 crossingPoint = from.add(to.sub(from).mult(fromDistance / distanceDelta)).sub(portalPoint);

        Vector3 xAxis = localToWorld.transformDirection(Vector3.UnitX());
        if (Math.abs(Vector3.dot(crossingPoint, xAxis)) >= Vector3.dot(xAxis, xAxis)) {
            return null;
        }

        Vector3 yAxis = localToWorld.transformDirection(Vector3.UnitY());
        if (Math.abs(Vector3.dot(crossingPoint, yAxis)) >= Vector3.dot(yAxis, yAxis)) {
            return null;
        }

        return fromDistance > 0.0f ? front : back;
    }

    public float distTo(Vector3 point) {
        Matrix4 localToWorld = localToWorld();
        Vector3 v = point.sub(localToWorld.translation());

        Vector3 xAxis = localToWorld.transformDirection(Vector3.UnitX());
        Vector3 yAxis = localToWorld.transformDirection(Vector3.UnitY());

        float px = clamp(Vector3.dot(v, xAxis) / Vector3.dot(xAxis, xAxis), -1.0f, 1.0f);
        float py = clamp(Vector3.dot(v, yAxis) / Vector3.dot(yAxis, yAxis), -1.0f, 1.0f);
        Vector3 closest = xAxis.mult(px).add(yAxis.mult(py));

        return v.sub(closest).length();
    }

    public static void connect(Portal a, Portal b) {
        if (a == null || b == null) {
            return;
        }

        connect(a.front, b.back);
        connect(b.front, a.back);
    }

    public static void connect(Warp a, Warp b) {
        if (a == null || b == null || a.fromPortal == null || b.fromPortal == null) {
            return;
        }

        a.toPortal = b.fromPortal;
        b.toPortal = a.fromPortal;

        a.delta = a.fromPortal.localToWorld().mult(b.fromPortal.worldToLocal());
        b.delta = b.fromPortal.localToWorld().mult(a.fromPortal.worldToLocal());
        a.deltaInv = b.delta;
        b.deltaInv = a.delta;
    }

    private static Material defaultMaterial() {
        PortalMaterial material = new PortalMaterial();
        material.setFallbackTexture(new Texture("/textures/white.bmp"));
        return material;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static class Warp {
        public Matrix4 delta = Matrix4.Identity();
        public Matrix4 deltaInv = Matrix4.Identity();
        public final Portal fromPortal;
        public Portal toPortal;

        private Warp(Portal fromPortal) {
            this.fromPortal = fromPortal;
        }
    }
}
