package org.example.engine.material;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.raytracing.RayTracingSceneBuffers;

import java.util.Collections;
import java.util.List;

public class RayTracingMaterial extends Material {
    private static final int MAX_DIFFUSE_TEXTURES = 31;
    private ComputeBuffer triangleBuffer;
    private ComputeBuffer sphereBuffer;
    private ComputeBuffer nodeBuffer;
    private ComputeBuffer cornellBoxBuffer;
    private ComputeBuffer materialBuffer;
    private List<Texture> diffuseTextures = Collections.emptyList();
    private Texture lastFrame;
    private Vector3 cameraPosition = new Vector3(0.0f);
    private Matrix4 inverseProjection = Matrix4.Identity();
    private Matrix4 cameraToWorld = Matrix4.Identity();
    private int triangleCount;
    private int sphereCount;
    private int cornellTriangleCount;
    private int materialCount;
    private int maxBounces = 4;
    private int screenWidth = 1;
    private int screenHeight = 1;
    private float accumulationBias;
    private boolean darkBackground;

    public RayTracingMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public RayTracingMaterial setMaxBounces(int maxBounces) {
        this.maxBounces = Math.max(1, maxBounces);
        return this;
    }

    public RayTracingMaterial setSceneBuffers(RayTracingSceneBuffers buffers) {
        if (buffers == null) {
            triangleBuffer = null;
            triangleCount = 0;
            sphereBuffer = null;
            sphereCount = 0;
            nodeBuffer = null;
            cornellBoxBuffer = null;
            cornellTriangleCount = 0;
            materialBuffer = null;
            materialCount = 0;
            diffuseTextures = Collections.emptyList();
            return this;
        }

        triangleBuffer = buffers.triangleBuffer;
        triangleCount = buffers.triangleCount;
        sphereBuffer = buffers.sphereBuffer;
        sphereCount = buffers.sphereCount;
        nodeBuffer = buffers.nodeBuffer;
        cornellBoxBuffer = buffers.cornellBoxBuffer;
        cornellTriangleCount = buffers.cornellTriangleCount;
        materialBuffer = buffers.materialBuffer;
        materialCount = buffers.materialCount;
        diffuseTextures = buffers.diffuseTextures == null ? Collections.emptyList() : buffers.diffuseTextures;
        return this;
    }

    public RayTracingMaterial setLastFrame(Texture lastFrame) {
        this.lastFrame = lastFrame;
        return this;
    }

    public RayTracingMaterial setCamera(Vector3 cameraPosition, Matrix4 inverseProjection, Matrix4 cameraToWorld) {
        if (cameraPosition != null) {
            this.cameraPosition = cameraPosition;
        }
        if (inverseProjection != null) {
            this.inverseProjection = inverseProjection;
        }
        if (cameraToWorld != null) {
            this.cameraToWorld = cameraToWorld;
        }
        return this;
    }

    public RayTracingMaterial setResolution(int width, int height) {
        screenWidth = Math.max(1, width);
        screenHeight = Math.max(1, height);
        return this;
    }

    public RayTracingMaterial setAccumulationBias(float accumulationBias) {
        this.accumulationBias = Math.max(0.0f, accumulationBias);
        return this;
    }

    public RayTracingMaterial setDarkBackground(boolean darkBackground) {
        this.darkBackground = darkBackground;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (triangleBuffer != null && triangleBuffer.isValid()) {
            triangleBuffer.bindBase(0);
        }
        if (sphereBuffer != null && sphereBuffer.isValid()) {
            sphereBuffer.bindBase(1);
        }
        if (nodeBuffer != null && nodeBuffer.isValid()) {
            nodeBuffer.bindBase(2);
        }
        if (cornellBoxBuffer != null && cornellBoxBuffer.isValid()) {
            cornellBoxBuffer.bindBase(3);
        }
        if (materialBuffer != null && materialBuffer.isValid()) {
            materialBuffer.bindBase(4);
        }

        setVector3ToUniform("camPos", cameraPosition);
        setMatrix4ToUniform("invProject", inverseProjection);
        setMatrix4ToUniform("camToWorld", cameraToWorld);
        setVector2ToUniform("resolution", screenWidth, screenHeight);
        setFloatToUniform("rbias", accumulationBias);
        setIntToUniform("numTriangles", triangleCount);
        setIntToUniform("sphereCount", sphereCount);
        setIntToUniform("cornellTriangleCount", cornellTriangleCount);
        setIntToUniform("materialCount", materialCount);
        setIntToUniform("maxBounces", maxBounces);
        setIntToUniform("dark", darkBackground ? 1 : 0);
        setTexture("lastFrame", lastFrame, 0);
        bindDiffuseTextures();
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        int count = Math.min(diffuseTextures.size(), MAX_DIFFUSE_TEXTURES);
        for (int i = 0; i < count; i++) {
            unbindTexture(i + 1);
        }
    }

    private void bindDiffuseTextures() {
        int count = Math.min(diffuseTextures.size(), MAX_DIFFUSE_TEXTURES);
        setIntToUniform("diffuseTextureCount", count);
        for (int i = 0; i < count; i++) {
            Texture texture = diffuseTextures.get(i);
            if (texture != null && texture.isUploaded()) {
                setTexture("diffuseTextures[" + i + "]", texture, i + 1);
            }
        }
    }
}
