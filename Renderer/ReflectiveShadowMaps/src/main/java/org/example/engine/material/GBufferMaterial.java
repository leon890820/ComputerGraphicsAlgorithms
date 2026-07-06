package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Camera;

public class GBufferMaterial extends Material {
    public GBufferMaterial(String frag) {
        super(frag);
    }

    public GBufferMaterial(String frag, String vert) {
        super(frag, vert);
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

        Matrix4 model = go.localToWorld();
        Camera camera = ctx.camera;
        Matrix4 view = camera.getViewMatrix();
        Matrix4 project = camera.getProjectionMatrix();

        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("viewMatrix", view);
        setMatrix4ToUniform("projectMatrix", project);
        applySkinning(go, subMesh);

        if (subMesh != null && subMesh.textureKa != null && subMesh.textureKa.isUploaded()) {
            setTexture("tex", subMesh.textureKa, 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
