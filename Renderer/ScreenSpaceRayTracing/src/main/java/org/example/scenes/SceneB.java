package org.example.scenes;

import org.example.engine.gameobject.AssimpObject;
import org.example.engine.gameobject.GameObject;
import org.example.engine.gameobject.PhongObject;
import org.example.engine.gl.Texture;
import org.example.engine.light.Light;
import org.example.engine.light.PointLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.material.SSRMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneB implements IScene {
    private final Vector3 pointA = new Vector3(0, -660f, -800);
    private final Vector3 pointB = new Vector3(0, -660f, 800);

    private Vector3 currentTarget = pointB;
    private final float speed = 200.0f;
    private float rotation = 0.0f;

    private AssimpObject gura;
    private Light light;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 45.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 10000.0f);
        camera.transform.setPosition(0.0f, -500f, 1100.0f).setEular(-0.1f, 0.0f, 0.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        PhongMaterial phongMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        PhongObject sponza = new PhongObject("../../Model/sponza/Scale300Sponza", phongMaterial);
        gura = new AssimpObject("../../Model/GuraAnim/gura.glb", phongMaterial);
        gura.playAnimation("ParadeWalk");
        gura.setScale(50, 50, 50).setPosition(0, -660, 100);

        SSRMaterial ssrMaterial =
                new SSRMaterial("/shaders/ScreenSpaceRayTracing.frag", "/shaders/ScreenSpaceRayTracing.vert");
        ssrMaterial
                .setNormalMap(new Texture("/textures/Wall03_Normal.jpg"))
                .setFuzz(0.0f)
                .setFuzzySampleCount(1);

        PhongObject ssrFloor = new PhongObject("/meshes/quad", ssrMaterial);
        ssrFloor.setEular(-3.1415926f / 2, 0, 0)
                .setScale(1500, 1500, 1500)
                .setPosition(0, -659f, 0);

        sponza.setScene(scene);
        gura.setScene(scene);
        ssrFloor.setScene(scene);

        scene.addObject(sponza);
        scene.addObject(gura);
        scene.addObject(ssrFloor);

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
        if (gura != null) {
            gura.updateAnimation(time);
            updateFurinaMovement(gura, 0.016f);
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

        //furina.setEular(0, rotation, 0);
        rotation += 0.02f;
    }
}

