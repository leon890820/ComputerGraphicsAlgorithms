package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.light.DirectionalLight;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.math.Vector3;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;

public class FinalSceneMaterial extends Material {

    private Texture albedoTex;
    private Texture normalTex;
    private Texture positionTex;
    private Texture rawSSAOTex;
    private Texture ssaoTex;
    private Texture edgeTex;
    private Light light;
    private Vector3 viewPosition = Vector3.Zero();
    private float time;
    private int lightType;
    private boolean useSSAO = true;

    public FinalSceneMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public FinalSceneMaterial setInputs(GBuffer gBuffer, Texture rawSSAOTexture, Texture ssaoTexture, Texture edgeTexture) {
        if (gBuffer != null) {
            albedoTex = gBuffer.albedo;
            normalTex = gBuffer.normal;
            positionTex = gBuffer.position;
        }

        rawSSAOTex = rawSSAOTexture;
        ssaoTex = ssaoTexture;
        edgeTex = edgeTexture;
        return this;
    }

    public FinalSceneMaterial setRenderState(RenderContext ctx, Light light) {
        this.light = light;
        time = ctx == null ? 0.0f : ctx.time;
        if (ctx != null && ctx.camera != null) {
            viewPosition = ctx.camera.transform.position;
        }

        if (light instanceof PointLight) {
            lightType = 1;
        } else if (light instanceof DirectionalLight) {
            lightType = 2;
        } else {
            lightType = 0;
        }

        return this;
    }

    public FinalSceneMaterial setUseSSAO(boolean enable) {
        useSSAO = enable;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        setTexture("albedoTex", albedoTex, 0);
        setTexture("normalTex", normalTex, 1);
        setTexture("positionTex", positionTex, 2);
        setTexture("rawSSAOTex", rawSSAOTex, 3);
        setTexture("ssaoTex", ssaoTex, 4);
        setTexture("edgeTex", edgeTex, 5);

        setFloatToUniform("time", time);
        setIntToUniform("lightType", lightType);
        setIntToUniform("useSSAO", useSSAO ? 1 : 0);
        setVector3ToUniform("view_pos", viewPosition);

        if (light != null) {
            setVector3ToUniform("light_pos", light.transform.position);
            setVector3ToUniform("light_dir", light.getLightDir());
            setVector3ToUniform("light_color", light.getLightColor());
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        unbindTexture(1);
        unbindTexture(2);
        unbindTexture(3);
        unbindTexture(4);
        unbindTexture(5);
    }
}
