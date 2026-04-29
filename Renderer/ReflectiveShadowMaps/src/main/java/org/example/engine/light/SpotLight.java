package org.example.engine.light;

import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.material.LightMaterial;
import org.example.engine.render.RenderContext;
import org.example.engine.render.Renderer;
import org.example.engine.gl.Texture;

public class SpotLight extends Light {

    public float fov = 60.0f;
    public float aspect = 1.0f;
    public float near = 0.1f;
    public float far = 10000.0f;

    public float cutoff = 12.5f;
    public float outerCutoff = 17.5f;

    public SpotLight(Vector3 pos, Vector3 dir, Vector3 c) {
        super(pos, dir, c);
    }

    @Override
    public Light setPerspective(float fov, float aspect, float near, float far) {
        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
        return this;
    }

    public SpotLight setCutoff(float cutoff, float outerCutoff) {
        this.cutoff = cutoff;
        this.outerCutoff = outerCutoff;
        return this;
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return Matrix4.Perspective(fov, aspect, near, far);
    }

    @Override
    public void setShaderParameter(LightMaterial material) {
        Matrix4 view = getViewMatrix();
        Matrix4 project = getProjectionMatrix();

        material.setMatrix4ToUniform("lightSpaceMatrix", project.mult(view));
        material.setVector3ToUniform("light_color", light_color);
        material.setVector3ToUniform("light_dir", light_dir);
        material.setVector3ToUniform("light_pos", transform.position);
        material.setFloatToUniform("lightFar", far);
    }

    @Override
    public float getLightFar() {
        return far;
    }

//    @Override
//    public void renderShadow(RenderContext ctx, Renderer renderer) {
//        renderer.shadowPass.render(ctx);
//    }
//
//    @Override
//    public void renderLighting(RenderContext ctx, Renderer renderer) {
//        Texture[] buffer = renderer.gBufferPass.getBuffer();
//        Texture depth = renderer.shadowPass.getDepthBuffer();
//        renderer.spotScenePass.setGBuffer(
//                buffer[0], buffer[1], buffer[2], depth
//        );
//
//        renderer.spotScenePass.render(ctx);
//    }
}