package org.example.scenes;

import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public interface IScene {
    Scene load(Camera camera, int screenWidth, int screenHeight);

    void update(float time);

    float getWalkSpeed();
}
