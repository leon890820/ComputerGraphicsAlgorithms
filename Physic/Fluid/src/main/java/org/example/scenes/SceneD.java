package org.example.scenes;

import org.example.engine.gameobject.ParticleDisplay3D;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneD implements IScene {
    private static final int GRID_SIZE = 5;
    private static final float INITIAL_SPACING = 0.35f;
    private static final int SPHERE_RESOLUTION = 3;
    private static final float PARTICLE_RADIUS = 0.055f;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        camera.setPositionOrientation(new Vector3(0.0f, 0.0f, 3.0f), 0.0f, 0.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);
        scene.setRenderMode(Scene.RenderMode.PARTICLE_EXAMPLE);
        scene.addObject(new ParticleDisplay3D(
                GRID_SIZE,
                INITIAL_SPACING,
                SPHERE_RESOLUTION,
                PARTICLE_RADIUS
        ));
        return scene;
    }

    @Override
    public void update(float time) {
    }

    @Override
    public float getWalkSpeed() {
        return 0.03f;
    }
}