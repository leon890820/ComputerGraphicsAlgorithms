package org.example.scenes;

import org.example.engine.gameobject.GameObject;
import org.example.engine.gameobject.PhongObject;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneB implements IScene {
    private final Vector3 pointA = new Vector3(0, -660f, -800);
    private final Vector3 pointB = new Vector3(0, -660f, 800);

    private Vector3 currentTarget = pointB;
    private final float speed = 200.0f;
    private float rotation = 0.0f;

    private PhongObject furina;
    private Light light;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        camera.setSize(screenWidth, screenHeight, 0.1f, 10000.0f);
        camera.transform.setPosition(0.0f, -300f, 800.0f).setEular(-0.1f, 0.0f, 0.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        PhongMaterial phongMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        PhongObject sponza = new PhongObject("../../Model/sponza/Scale300Sponza", phongMaterial);
        furina = new PhongObject("../../Model/Furina/Furina", phongMaterial);
        furina.setScale(100, 100, 100).setPosition(0, -660, 100);

        sponza.setScene(scene);
        furina.setScene(scene);

        scene.addObject(sponza);
        scene.addObject(furina);

        light = new PointLight(
                new Vector3(0, 0, 0),
                new Vector3(0.8f, 0.8f, 0.8f)
        );
        scene.addLight(light);

        currentTarget = pointB;
        rotation = 0.0f;

        return scene;
    }

    @Override
    public void update(float time) {
        if (furina != null) {
            updateFurinaMovement(furina, 0.016f);
        }
    }

    @Override
    public float getWalkSpeed() {
        return 3.0f;
    }

    private void updateFurinaMovement(GameObject furina, float dt) {
        Vector3 pos = furina.transform.position;
        Vector3 dir = currentTarget.sub(pos);
        float dist = dir.length();

        if (dist < 1.0f) {
            if (currentTarget == pointA) {
                currentTarget = pointB;
            } else {
                currentTarget = pointA;
            }
            return;
        }

        dir = dir.unit_vector();
        float step = speed * dt;

        if (step > dist) {
            furina.setPosition(currentTarget.copy());
        } else {
            furina.setPosition(pos.add(dir.mult(step)));
        }

        furina.setEular(0, rotation, 0);
        rotation += 0.02f;
    }
}
