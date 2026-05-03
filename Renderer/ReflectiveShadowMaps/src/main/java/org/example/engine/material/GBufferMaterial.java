package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;
import org.example.engine.scene.Camera;

public class GBufferMaterial extends Material {
    public GBufferMaterial(String frag) {
        super(frag);
    }

    public GBufferMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public void run(GameObject go, SubMesh subMesh) {
        Matrix4 model = go.localToWorld();
        Camera camera = go.scene.getCamera();
        Matrix4 view = camera.getViewMatrix();
        Matrix4 project = camera.getProjectionMatrix();

        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("viewMatrix", view);
        setMatrix4ToUniform("projectMatrix", project);

        if (subMesh != null && subMesh.textureKa != null && subMesh.textureKa.isUploaded()) {
            setTexture("tex", subMesh.textureKa, 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
