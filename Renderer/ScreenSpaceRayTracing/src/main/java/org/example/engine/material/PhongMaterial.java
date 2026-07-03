package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.SubMesh;
import org.example.engine.gameobject.GameObject;
import org.example.engine.render.RenderContext;
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
        run(null, go, subMesh);
    }

    @Override
    public void run(RenderContext ctx, GameObject go, SubMesh subMesh) {
        if (ctx == null || ctx.camera == null) {
            System.out.println("[PhongMaterial] RenderContext camera is not set.");
            return;
        }

        Matrix4 model = go.localToWorld();
        Camera camera = ctx.camera;
        Matrix4 mvp = camera.Matrix().mult(model);

        setMatrix4ToUniform("MVP", mvp);
        setMatrix4ToUniform("modelMatrix", model);

        setVector3ToUniform("ambient_light", new Vector3(0.5f,0.5f,0.5f));

        setVector3ToUniform("view_pos", camera.transform.position);

        setVector3ToUniform("light_pos", lightSource.transform.position);
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
