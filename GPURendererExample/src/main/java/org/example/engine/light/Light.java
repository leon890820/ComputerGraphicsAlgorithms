package org.example.engine.light;

import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.render.RenderContext;
import org.example.engine.render.Renderer;
import org.example.engine.gameobject.GameObject;
import org.example.engine.material.LightMaterial;

public abstract class Light extends GameObject {

    public Vector3 light_dir;
    public Vector3 light_color;

    protected float intensity = 1.0f;
    protected boolean castShadow = true;
    protected Vector3 up = new Vector3(0, 1, 0);

    // ===== Projection params（統一存在 base）=====
    protected float fov, aspect, near, far;
    protected float left, right, bottom, top;

    protected boolean usePerspective = false;
    protected boolean useOrtho = false;

    public Light(Vector3 pos, Vector3 ld, Vector3 lc) {
        this.transform.position = pos;
        this.light_dir = ld.unit_vector();
        this.light_color = lc;
    }

    public Light setLightdirection(Vector3 v) {
        this.light_dir = v.unit_vector();
        return this;
    }

    public Light setLightdirection(float x, float y, float z) {
        this.light_dir.set(x, y, z);
        this.light_dir.normalize();
        return this;
    }

    public Matrix4 getViewMatrix() {
        return Matrix4.LookAt(
                transform.position,
                transform.position.add(light_dir),
                up
        );
    }

    // ===== Perspective =====
    public Light setPerspective(float fov, float aspect, float near, float far) {
        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;

        usePerspective = true;
        useOrtho = false;

        return this;
    }

    // ===== Ortho =====
    public Light setOrtho(float left, float right, float bottom, float top, float near, float far) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;

        useOrtho = true;
        usePerspective = false;

        return this;
    }

    // ===== 共用 helper（給子類用）=====
    protected Matrix4 buildProjectionMatrix() {
        if (usePerspective) {
            return Matrix4.Perspective(fov, aspect, near, far);
        }

        if (useOrtho) {
            return Matrix4.Ortho(left, right, bottom, top, near, far);
        }

        // fallback
        return Matrix4.Identity();
    }

    // ===== 抽象（維持原接口）=====
    public abstract Matrix4 getProjectionMatrix();

    public abstract void setShaderParameter(LightMaterial material);

    public abstract float getLightFar();

    public abstract void renderShadow(RenderContext ctx, Renderer renderer);

    public abstract void renderLighting(RenderContext ctx, Renderer renderer);

    // ===== Getter =====
    public float getIntensity() {
        return intensity;
    }

    public boolean isCastShadow() {
        return castShadow;
    }

    public Vector3 getLightColor() {
        return light_color;
    }

    public Vector3 getLightDir() {
        return light_dir;
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }
}