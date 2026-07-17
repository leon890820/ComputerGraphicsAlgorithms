package org.example.scenes;

import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneD implements IScene {
    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        camera.transform.setPosition(0, 0, 3.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);
        scene.setRenderMode(Scene.RenderMode.COMPUTE_EXAMPLE);
        return scene;
    }

    @Override
    public void update(float time) {
    }

    @Override
    public float getWalkSpeed() {
        return 0.0f;
    }
}