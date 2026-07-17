package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.gl.TextureCube;
import org.example.engine.math.Vector3;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;

public class PostProcessMaterial extends Material {

    private Texture sourceTexture;
    private TextureCube skybox;

    public PostProcessMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public PostProcessMaterial setSourceTexture(Texture sourceTexture) {
        this.sourceTexture = sourceTexture;
        return this;
    }

    public PostProcessMaterial setSkybox(TextureCube skybox) {
        this.skybox = skybox;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        setTexture("sceneTexture", sourceTexture, 0);
        setCubeTexture("skybox", skybox, 1);

        if (data == null || data.camera == null) {
            return;
        }

        setMatrix4ToUniform("inverseProjection", data.camera.getProjectionMatrix().Inverse());
        setMatrix4ToUniform("inverseView", data.camera.getViewMatrix().Inverse());

        Vector3 cameraPosition = data.camera.transform.position;
        setVector3ToUniform("cameraPosition", cameraPosition);
        setVector3ToUniform("blackholePosition", 0.0f, 0.0f, 0.0f);
        setFloatToUniform("schwarzschildRadius", 1.15f);
        setFloatToUniform("stepSize", 0.05f);
        setIntToUniform("maxSteps", 1024);
        setFloatToUniform("innerRadius", 1.35f);
        setFloatToUniform("outerRadius", 18.0f);
        setFloatToUniform("thickness", 0.5f);
        setFloatToUniform("density", 0.5f);
        setFloatToUniform("diskFalloffPower", 1.5f);
        setFloatToUniform("diskMinBrightness", 0.01f);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        glActiveTexture(GL_TEXTURE0 + 1);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}