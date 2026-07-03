package org.example.scenes;

import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public interface IScene {
    Scene load(Camera camera);

    void update(float time);
}
