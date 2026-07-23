package org.example.engine.material;

import org.example.engine.gl.Texture;

import java.util.Set;

import static org.lwjgl.opengl.GL33.GL_BLEND;
import static org.lwjgl.opengl.GL33.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL33.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL33.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL33.glBlendFunc;
import static org.lwjgl.opengl.GL33.glDisable;
import static org.lwjgl.opengl.GL33.glEnable;

public class FaceMaterial extends Material {
    private static final int MAX_BONES = 100;

    private final Texture faceTexture;
    private float maskThreshold = 0.08f;

    public FaceMaterial(Texture faceTexture) {
        super("/shaders/core/face.frag", "/shaders/core/BlinnPhong.vert");
        this.faceTexture = faceTexture;
    }

    public FaceMaterial setMaskThreshold(float threshold) {
        maskThreshold = threshold;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.mvpMatrix == null) {
            System.out.println("[FaceMaterial] render data is missing model or MVP matrix.");
            return;
        }

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        setMatrix4ToUniform("MVP", data.mvpMatrix);
        setMatrix4ToUniform("modelMatrix", data.modelMatrix);

        boolean useSkinning = data.boneMatrices != null && data.boneMatrices.length > 0;
        setIntToUniform("useSkinning", useSkinning ? 1 : 0);
        if (useSkinning) {
            setMatrix4ArrayToUniform("boneMatrices[0]", data.boneMatrices, MAX_BONES);
        }

        setVector3ToUniform("view_pos", data.viewPosition);
        setVector4ToUniform("eyeColor", 1.0f, 1.0f, 1.0f, 1.0f);
        setVector4ToUniform("rimColor", 0.55f, 0.85f, 1.0f, 0.2f);
        setFloatToUniform("maskThreshold", maskThreshold);
        setTexture("faceTexture", faceTexture, 0);
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        glDisable(GL_BLEND);
    }

    @Override
    public void collectTextures(Set<Texture> textures) {
        if (faceTexture != null) {
            textures.add(faceTexture);
        }
    }
}
