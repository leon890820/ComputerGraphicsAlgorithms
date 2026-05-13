package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.SubMesh;
import org.example.engine.gameobject.GameObject;
import org.example.engine.scene.Camera;

public class PhongMaterial extends Material {

    Texture texture;

    public PhongMaterial(String frag) {
        super(frag);
    }

    public PhongMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public PhongMaterial setTexture(Texture t) {
        texture = t;
        return this;
    }

    @Override
    public void run(GameObject go, SubMesh subMesh) {
        Matrix4 model = go.localToWorld();
        Matrix4 mvp = go.MVP();

        setMatrix4ToUniform("MVP", mvp);
        setMatrix4ToUniform("modelMatrix", model);

        setVector3ToUniform("ambient_light", new Vector3(0.5f,0.5f,0.5f));

        Camera camera = go.scene.getCamera();
        setVector3ToUniform("view_pos", camera.transform.position);

        setVector3ToUniform("light_color", lightSource.light_color);
        setVector3ToUniform("light_dir", lightSource.light_dir);
        setVector3ToUniform("light_color", lightSource.light_color);


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
