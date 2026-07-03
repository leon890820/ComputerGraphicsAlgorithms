package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;


public class SSRMaterial extends Material{

    Texture albedoTex;
    Texture normalTex;
    Texture worldPosTex;
    Texture depthTex;
    Texture normalMapTex;
    float fuzz = 0.0f;
    int fuzzySampleCount = 1;
    public SSRMaterial(String frag) {
        super(frag);
    }

    public SSRMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public SSRMaterial setAlbedoTex(Texture t) {
        albedoTex = t;
        return this;
    }
    public SSRMaterial setNormalTex(Texture t) {
        normalTex = t;
        return this;
    }
    public SSRMaterial setWorldPosTex(Texture t) {
        worldPosTex = t;
        return this;
    }
    public SSRMaterial setDepthTex(Texture t) {
        depthTex = t;
        return this;
    }

    public SSRMaterial setNormalMap(Texture t) {
        normalMapTex = t;
        return this;
    }

    public Texture getNormalMap() {
        return normalMapTex;
    }

    public SSRMaterial setFuzz(float fuzz) {
        this.fuzz = Math.max(0.0f, fuzz);
        return this;
    }

    public SSRMaterial setFuzzySampleCount(int sampleCount) {
        fuzzySampleCount = Math.max(1, sampleCount);
        return this;
    }

    @Override
    public void run(GameObject go, SubMesh subMesh) {
        run(null, go, subMesh);
    }

    @Override
    public void run(RenderContext ctx, GameObject go, SubMesh subMesh) {
        if (ctx == null || ctx.camera == null) {
            System.out.println("[SSRMaterial] RenderContext camera is not set.");
            return;
        }

        Camera camera = ctx.camera;
        Matrix4 model = go.localToWorld();
        Matrix4 V = camera.getViewMatrix();
        Matrix4 P = camera.getProjectionMatrix();

        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("u_ViewMatrix", V);
        setMatrix4ToUniform("u_ProjectionMatrix", P);

        setTexture("albedoTex", albedoTex, 0);
        setTexture("normalTex", normalTex, 1);
        setTexture("worldPosTex", worldPosTex, 2);
        setTexture("depthTex", depthTex, 3);
        if (normalMapTex != null && normalMapTex.isUploaded()) {
            setIntToUniform("hasNormalMap", 1);
            setTexture("floorNormalMap", normalMapTex, 4);
        } else {
            setIntToUniform("hasNormalMap", 0);
        }

        setVector3ToUniform("cameraPos", camera.transform.position);
        setFloatToUniform("cameraFar", camera.getFar());
        setFloatToUniform("u_WindowWidth", ctx.screenWidth);
        setFloatToUniform("u_WindowHeight", ctx.screenHeight);
        setFloatToUniform("u_Fuzz", fuzz);
        setIntToUniform("u_FuzzySampleCount", fuzzySampleCount);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        unbindTexture(1);
        unbindTexture(2);
        unbindTexture(3);
        unbindTexture(4);
    }
}
