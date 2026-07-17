package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.gl.TextureCube;

import java.util.Set;

import static org.lwjgl.opengl.GL33.*;

public class SkyBoxMaterial extends Material {

    private TextureCube skybox;

    public SkyBoxMaterial(String frag) {
        super(frag);
    }
    public SkyBoxMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public SkyBoxMaterial setSkybox(TextureCube t) {
        skybox = t;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.camera == null) {
            System.out.println("[SkyBoxMaterial] render data is missing camera.");
            return;
        }

        setCubeTexture("skybox", skybox, 0);
        setMatrix4ToUniform("inverseProjection", data.camera.getProjectionMatrix().Inverse());
        setMatrix4ToUniform("inverseView", data.camera.getViewMatrix().Inverse());
    }

    @Override
    public void cleanup() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        unbindTexture(0);
    }

    @Override
    public void collectTextures(Set<Texture> textures) {

    }

    @Override
    public void dispose() {
        if (skybox != null) {
            skybox.dispose();
            skybox = null;
        }
        super.dispose();
    }
}
