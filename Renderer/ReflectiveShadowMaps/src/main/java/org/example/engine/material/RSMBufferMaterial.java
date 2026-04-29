package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;

public class RSMBufferMaterial extends Material {
    public RSMBufferMaterial(String frag) {
        super(frag);
    }

    public RSMBufferMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public void run(GameObject go, SubMesh subMesh) {
        var camera = go.scene.getCamera();
        Matrix4 model = go.localToWorld();
        Matrix4 view = camera.getViewMatrix();
        Matrix4 lightView = lightSource.getViewMatrix();
        Matrix4 lightProject = lightSource.getProjectionMatrix();

        setMatrix4ToUniform("modelMatrix", model);
        setMatrix4ToUniform("viewMatrix", view);
        setMatrix4ToUniform("lightVPMatrix", lightProject.mult(lightView));

        if (subMesh != null && subMesh.textureKa != null && subMesh.textureKa.isUploaded()) {
            setTexture("tex", subMesh.textureKa, 0);
        }
    }

    @Override
    public void cleanup() {
    }
}