package org.example.scenes;

import org.example.engine.gameobject.PhongObject;
import org.example.engine.gl.Texture;
import org.example.engine.light.PointLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneB implements IScene {
    private PointLight light;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        light = new PointLight(
                new Vector3(-6, 6, 4),
                new Vector3(0.4f, 0.7f, 1.0f)
        );

        PhongMaterial objectMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        PhongObject leftObject =
                new PhongObject("/meshes/Furina/Furina", objectMaterial);
        leftObject.setScale(0.8f, 0.8f, 0.8f)
                .setPosition(-1.2f, 0.0f, 0.0f);

        PhongMaterial rightMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        PhongObject rightObject =
                new PhongObject("/meshes/Furina/Furina", rightMaterial);
        rightObject.setScale(0.8f, 0.8f, 0.8f)
                .setPosition(1.2f, 0.0f, -0.5f);

        PhongMaterial floorMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");
        floorMaterial.setTexture(new Texture("/textures/test.png"));

        PhongObject floor = new PhongObject("/meshes/quad", floorMaterial);
        floor.setEular(3.1415926f / 2, 0, 0)
                .setScale(4, 4, 4)
                .setPosition(0, -0.1f, 0);

        scene.addObject(leftObject);
        scene.addObject(rightObject);
        scene.addObject(floor);
        scene.addLight(light);

        return scene;
    }

    @Override
    public void update(float time) {
        if (light == null) {
            return;
        }

        light.setPosition(
                (float) Math.sin(time) * 6,
                6f,
                (float) Math.cos(time) * 6
        );
    }

    @Override
    public float getWalkSpeed() {
        return 0.05f;
    }
}
