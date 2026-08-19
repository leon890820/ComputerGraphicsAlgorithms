package org.example.scenes;

import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneB implements IScene {
    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        return Level1SceneBuilder.load(
                camera,
                screenWidth,
                screenHeight,
                Level1SceneBuilder.ViewPreset.LEFT_TUNNEL
        );
    }

    @Override
    public void update(float time) {
    }

    @Override
    public float getWalkSpeed() {
        return 2.9f;
    }
}
