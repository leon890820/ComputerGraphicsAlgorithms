package org.example.engine.material;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.Texture;
import org.example.engine.gl.Texture3D;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class FluidMarchingGpuMaterial extends Material {
    private ComputeBuffer triangleBuffer;
    private Texture3D densityTexture;
    private Texture sceneColorTexture;
    private Texture sceneDepthTexture;
    private Matrix4 viewProjection;
    private Vector3 cameraPosition;
    private Vector3 boundsCenter = new Vector3(0.0f);
    private Vector3 boundsSize = new Vector3(1.0f);
    private int screenWidth = 1;
    private int screenHeight = 1;
    private float cameraNear = 0.1f;
    private float cameraFar = 1000.0f;
    private Vector3 fluidColor = new Vector3(0.0f, 1.0f, 0.74f);
    private Vector3 rimColor = new Vector3(0.45f, 1.0f, 0.82f);
    private float rimPower = 2.4f;
    private float rimStrength = 0.85f;
    private float specularStrength = 0.55f;
    private float alpha = 0.86f;
    private float refractionStrength = 0.035f;
    private float depthFadeMultiplier = 2.8f;

    public FluidMarchingGpuMaterial() {
        super("/shaders/particle/shader/fluid_mesh.frag", "/shaders/particle/shader/fluid_marching_gpu.vert");
    }

    public FluidMarchingGpuMaterial setTriangleBuffer(ComputeBuffer triangleBuffer) {
        this.triangleBuffer = triangleBuffer;
        return this;
    }

    public FluidMarchingGpuMaterial setDensityTexture(Texture3D densityTexture) {
        this.densityTexture = densityTexture;
        return this;
    }

    public FluidMarchingGpuMaterial setBounds(Vector3 center, Vector3 size) {
        if (center != null) {
            boundsCenter = center;
        }
        if (size != null) {
            boundsSize = size;
        }
        return this;
    }

    public FluidMarchingGpuMaterial setViewProjection(Matrix4 viewProjection) {
        this.viewProjection = viewProjection;
        return this;
    }

    public FluidMarchingGpuMaterial setCameraPosition(Vector3 cameraPosition) {
        this.cameraPosition = cameraPosition;
        return this;
    }

    public FluidMarchingGpuMaterial setSceneTextures(
            Texture sceneColorTexture,
            Texture sceneDepthTexture,
            int screenWidth,
            int screenHeight
    ) {
        this.sceneColorTexture = sceneColorTexture;
        this.sceneDepthTexture = sceneDepthTexture;
        this.screenWidth = Math.max(1, screenWidth);
        this.screenHeight = Math.max(1, screenHeight);
        return this;
    }

    public FluidMarchingGpuMaterial setCameraClip(float near, float far) {
        cameraNear = Math.max(0.0001f, near);
        cameraFar = Math.max(cameraNear + 0.0001f, far);
        return this;
    }

    public FluidMarchingGpuMaterial setRefraction(float refractionStrength, float depthFadeMultiplier) {
        this.refractionStrength = Math.max(0.0f, refractionStrength);
        this.depthFadeMultiplier = Math.max(0.0f, depthFadeMultiplier);
        return this;
    }

    public FluidMarchingGpuMaterial setSlimeSurface(
            Vector3 fluidColor,
            Vector3 rimColor,
            float rimPower,
            float rimStrength,
            float specularStrength,
            float alpha
    ) {
        if (fluidColor != null) {
            this.fluidColor = fluidColor;
        }
        if (rimColor != null) {
            this.rimColor = rimColor;
        }

        this.rimPower = Math.max(0.001f, rimPower);
        this.rimStrength = Math.max(0.0f, rimStrength);
        this.specularStrength = Math.max(0.0f, specularStrength);
        this.alpha = Math.max(0.0f, Math.min(alpha, 1.0f));
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (triangleBuffer == null || viewProjection == null || densityTexture == null || !densityTexture.isUploaded()) {
            return;
        }

        triangleBuffer.bindBase(0);
        densityTexture.bind(0);
        setIntToUniform("densityTexture", 0);

        boolean hasSceneTextures = sceneColorTexture != null
                && sceneDepthTexture != null
                && sceneColorTexture.isUploaded()
                && sceneDepthTexture.isUploaded();
        if (hasSceneTextures) {
            sceneColorTexture.bind(1);
            sceneDepthTexture.bind(2);
            setIntToUniform("sceneColorTexture", 1);
            setIntToUniform("sceneDepthTexture", 2);
        }

        setIntToUniform("hasSceneTextures", hasSceneTextures ? 1 : 0);
        setMatrix4ToUniform("viewProjection", viewProjection);
        setVector3ToUniform("cameraPosition", cameraPosition);
        setVector3ToUniform("boundsCenter", boundsCenter);
        setVector3ToUniform("boundsSize", boundsSize);
        setVector2ToUniform("screenSize", screenWidth, screenHeight);
        setFloatToUniform("cameraNear", cameraNear);
        setFloatToUniform("cameraFar", cameraFar);
        setVector3ToUniform("fluidColor", fluidColor);
        setVector3ToUniform("rimColor", rimColor);
        setFloatToUniform("rimPower", rimPower);
        setFloatToUniform("rimStrength", rimStrength);
        setFloatToUniform("specularStrength", specularStrength);
        setFloatToUniform("alpha", alpha);
        setFloatToUniform("refractionStrength", refractionStrength);
        setFloatToUniform("depthFadeMultiplier", depthFadeMultiplier);
    }

    @Override
    public void cleanup() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_3D, 0);
        glActiveTexture(GL_TEXTURE0 + 1);
        glBindTexture(GL_TEXTURE_2D, 0);
        glActiveTexture(GL_TEXTURE0 + 2);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
