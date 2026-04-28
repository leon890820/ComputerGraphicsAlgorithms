package org.example.engine.light;

import org.example.engine.math.*;
import org.example.engine.material.*;
import org.example.engine.gl.*;
import org.example.engine.render.*;

public class DirectionalLight extends Light {

    public float left = -5.0f;
    public float right = 5.0f;
    public float bottom = -5.0f;
    public float top = 5.0f;
    public float near = 0.01f;
    public float far = 200.0f;

    public DirectionalLight(Vector3 pos, Vector3 dir, Vector3 c) {
        super(pos, dir, c);
    }

    @Override
    public Light setOrtho(float left, float right, float bottom, float top, float near, float far) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;
        return this;
    }



    @Override
    public Matrix4 getProjectionMatrix() {
        return Matrix4.Ortho(left, right, bottom, top, near, far);
    }


    @Override
    public void setShaderParameter(LightMaterial material){
        Matrix4 view = getViewMatrix();
        Matrix4 project = getProjectionMatrix();
        material.setVector3ToUniform("light_color", light_color);
        material.setMatrix4ToUniform("lightSpaceMatrix", project.mult(view));
        material.setVector3ToUniform("light_dir", light_dir);
        material.setFloatToUniform("lightFar", far);
        material.setVector3ToUniform("light_pos", transform.position);
    }

    @Override
    public float getLightFar(){
        return far;
    }

    @Override
    public void renderShadow(RenderContext ctx, Renderer renderer) {
        renderer.shadowPass.render(ctx);
    }

    @Override
    public void renderLighting(RenderContext ctx, Renderer renderer) {
        Texture[] buffer = renderer.gBufferPass.getBuffer();
        Texture depth = renderer.shadowPass.getDepthBuffer();

        renderer.directionalScenePass.setGBuffer(
                buffer[0], buffer[1], buffer[2], depth
        );

        renderer.directionalScenePass.render(ctx);
    }

}
