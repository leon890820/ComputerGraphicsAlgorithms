package org.example.engine.render;

import org.example.engine.light.Light;
import org.example.engine.math.Matrix4;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class RenderContext {
    public final Scene scene;
    public final Camera camera;
    public final int screenWidth;
    public final int screenHeight;
    public Light activeLight;
    public Matrix4 activeShadowMatrix;

    public RenderContext(Scene scene, Camera camera, int screenWidth, int screenHeight) {
        this.scene = scene;
        this.camera = camera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
}
