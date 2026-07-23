package org.example.engine.material;

import org.example.engine.gl.Texture3D;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class FluidRayMarchMaterial extends Material {
    private Texture3D densityTexture;
    private Camera camera;
    private Vector3 boundsCenter = new Vector3(0.0f);
    private Vector3 boundsSize = new Vector3(1.0f);
    private float stepSize = 0.015f;
    private float densityMultiplier = 0.04f;
    private float densityOffset = 0.0f;

    public FluidRayMarchMaterial() {
        super("/shaders/particle/shader/fluid_raymarch.frag", "/shaders/core/quad.vert");
    }

    public FluidRayMarchMaterial setDensityTexture(Texture3D densityTexture) {
        this.densityTexture = densityTexture;
        return this;
    }

    public FluidRayMarchMaterial setCamera(Camera camera) {
        this.camera = camera;
        return this;
    }

    public FluidRayMarchMaterial setBounds(Vector3 center, Vector3 size) {
        if (center != null) {
            boundsCenter = center;
        }
        if (size != null) {
            boundsSize = size;
        }
        return this;
    }

    public FluidRayMarchMaterial setStepSize(float stepSize) {
        this.stepSize = Math.max(0.0001f, stepSize);
        return this;
    }

    public FluidRayMarchMaterial setDensityMultiplier(float densityMultiplier) {
        this.densityMultiplier = Math.max(0.0f, densityMultiplier);
        return this;
    }

    public FluidRayMarchMaterial setDensityOffset(float densityOffset) {
        this.densityOffset = densityOffset;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (densityTexture == null || !densityTexture.isUploaded() || camera == null) {
            System.out.println("[FluidRayMarchMaterial] Warning: missing density texture or camera");
            return;
        }

        Matrix4 inverseProjection = camera.getProjectionMatrix().Inverse();
        Matrix4 cameraToWorld = camera.transform.localToWorld();

        densityTexture.bind(0);
        setIntToUniform("densityTexture", 0);
        setMatrix4ToUniform("inverseProjection", inverseProjection);
        setMatrix4ToUniform("cameraToWorld", cameraToWorld);
        setVector3ToUniform("cameraPosition", camera.transform.position);
        setVector3ToUniform("boundsCenter", boundsCenter);
        setVector3ToUniform("boundsSize", boundsSize);
        setFloatToUniform("stepSize", stepSize);
        setFloatToUniform("densityMultiplier", densityMultiplier);
        setFloatToUniform("densityOffset", densityOffset);
    }

    @Override
    public void cleanup() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
