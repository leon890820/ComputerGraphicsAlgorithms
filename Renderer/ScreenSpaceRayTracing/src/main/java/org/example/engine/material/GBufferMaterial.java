package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.SubMesh;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

public class GBufferMaterial extends Material {

    Texture texture;
    public GBufferMaterial(String frag) {
        super(frag);
    }

    public GBufferMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public GBufferMaterial setTexture(Texture t) {
        texture = t;
        return this;
    }

    @Override
    public void run(GameObject go, SubMesh subMesh) {
        run(null, go, subMesh);
    }

    @Override
    public void run(RenderContext ctx, GameObject go, SubMesh subMesh) {
        if (ctx == null || ctx.camera == null) {
            System.out.println("[GBufferMaterial] RenderContext camera is not set.");
            return;
        }

        Camera camera = ctx.camera;
        Matrix4 model = go.localToWorld();
        Matrix4 mvp = camera.Matrix().mult(model);
        Matrix4 V = camera.getViewMatrix();

        setMatrix4ToUniform("MVP", mvp);
        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("u_ViewMatrix", V);

        setVector3ToUniform("ambient_light", new Vector3(0.5f,0.5f,0.5f));
        setVector3ToUniform("cameraPos", camera.transform.position);
        setFloatToUniform("cameraFar", camera.getFar());


        Texture useTex = texture;

        if (useTex == null && subMesh != null) {
            useTex = subMesh.textureKa;
        }

        if (useTex != null && useTex.isUploaded()) {
            setIntToUniform("hasTexture", 1);
            setTexture("tex", useTex, 0);
        } else {
            setIntToUniform("hasTexture", 0);
            setVector3ToUniform("defaultColor", new Vector3(0.45f, 0.45f, 0.45f));
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
