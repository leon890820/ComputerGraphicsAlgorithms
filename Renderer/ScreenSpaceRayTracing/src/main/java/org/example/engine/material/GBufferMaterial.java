package org.example.engine.material;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.SubMesh;
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
        Matrix4 model = go.localToWorld();
        Matrix4 mvp = go.MVP();
        var camera = go.scene.getCamera();

        setMatrix4ToUniform("MVP", mvp);
        setMatrix4ToUniform("modelMatrix", model);

        setVector3ToUniform("ambient_light", new Vector3(0.5f,0.5f,0.5f));
        setVector3ToUniform("cameraPos", camera.transform.position);
        setFloatToUniform("cameraFar", camera.getFar());
        Texture useTex = texture;

        if (useTex == null && subMesh != null) {
            useTex = subMesh.textureKa;
        }

        if (useTex != null && useTex.isUploaded()) {

            setTexture("tex", useTex, 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
    }
}
