package org.example.scenes;

import org.example.engine.gameobject.PRTObject;
import org.example.engine.light.PointLight;
import org.example.engine.material.PRTMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.prt.PRTBakeMode;
import org.example.engine.prt.PRTReflectionMode;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneA implements IScene {
    private static final String FURINA_MESH = "/meshes/Furina/Furina";

    private PointLight light;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 75.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        
        Scene scene = new Scene();
        scene.setCamera(camera);

        light = new PointLight(
                new Vector3(10, 10, 0),
                new Vector3(0.8f, 0.8f, 0.8f)
        );

        scene.addLight(light);

        PRTObject furina = new PRTObject(
                FURINA_MESH,
                new PRTMaterial(),
                3,
                128,
                PRTBakeMode.UNSHADOW,
                PRTReflectionMode.GLOSSY_MATRIX
        );
        furina.setPosition(0.0f, 0.0f, 0.0f);
        furina.setScale(1f, 1f, 1f);

        scene.addObject(furina);

        return scene;
    }

    @Override
    public void update(float time) {
        if (light == null) {
            return;
        }

        light.setPosition(
                (float) Math.cos(time) * 10,
                10f,
                (float) Math.sin(time) * 10
        );
    }

    @Override
    public float getWalkSpeed() {
        return 0.05f;
    }
}
