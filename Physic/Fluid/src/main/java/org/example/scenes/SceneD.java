package org.example.scenes;

import org.example.engine.gameobject.ParticleDisplay3D;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneD implements IScene {
    private ParticleDisplay3D particleDisplay;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        camera.setPositionOrientation(new Vector3(0.0f, 0.0f, 3.0f), 0.0f, 0.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);
        scene.setRenderMode(Scene.RenderMode.PARTICLE_EXAMPLE);
        particleDisplay = new ParticleDisplay3D();
        scene.addObject(particleDisplay);
        return scene;
    }

    @Override
    public void update(float time) {
    }

    @Override
    public float getWalkSpeed() {
        return 0.03f;
    }

    public void addColliderSize(float widthDelta, float heightDelta, float depthDelta) {
        if (particleDisplay == null) {
            return;
        }

        particleDisplay.getSimulator().addColliderSize(widthDelta, heightDelta, depthDelta);
    }

    public void addColliderUniformSize(float delta) {
        if (particleDisplay == null) {
            return;
        }

        particleDisplay.getSimulator().addColliderUniformSize(delta);
    }

    public void addColliderCenter(float xDelta, float yDelta, float zDelta) {
        if (particleDisplay == null) {
            return;
        }

        particleDisplay.getSimulator().addColliderCenter(xDelta, yDelta, zDelta);
    }
}
