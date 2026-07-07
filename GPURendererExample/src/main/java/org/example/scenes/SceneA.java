package org.example.scenes;

import org.example.engine.gameobject.MeshObject;
import org.example.engine.gl.Texture;
import org.example.engine.light.PointLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneA implements IScene {
    private PointLight light;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        light = new PointLight(
                new Vector3(10, 10, 0),
                new Vector3(0.8f, 0.8f, 0.8f)
        );

        PhongMaterial phongMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        MeshObject phongObject =
                new MeshObject("/meshes/Furina/Furina", phongMaterial);

        PhongMaterial floorMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        Texture floorTexture = new Texture("/textures/Floor.png");
        floorMaterial.setTexture(floorTexture);

        MeshObject floor = new MeshObject("/meshes/quad", floorMaterial);
        floor.setEular(3.1415926f / 2, 0, 0)
                .setScale(5, 5, 5)
                .setPosition(0, 0, 0);

        scene.addObject(phongObject);
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
