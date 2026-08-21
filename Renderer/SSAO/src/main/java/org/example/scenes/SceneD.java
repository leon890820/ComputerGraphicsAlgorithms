package org.example.scenes;

import org.example.engine.gameobject.MeshObject;
import org.example.engine.light.PointLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneD implements IScene {
    private PointLight keyLight;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 45.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 10000.0f);
        camera.transform.setPosition(0.0f, -500f, 1100.0f)
                .setEular(-0.1f, 0.0f, 0.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        PhongMaterial phongMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        MeshObject sponza = new MeshObject("../../Model/sponza/Scale300Sponza", phongMaterial);

        PhongMaterial guraMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");
        MeshObject gura = new MeshObject("../../Model/Hololive/gura.glb", guraMaterial);
        gura.setScale(50, 50, 50)
                .setPosition(0, -660, 100);

        scene.addObject(sponza);
        scene.addObject(gura);

        keyLight = new PointLight(
                new Vector3(0, -250, 550),
                new Vector3(1.4f, 1.4f, 1.4f)
        );
        keyLight.setRadius(1800.0f).setNearFar(1.0f, 3000.0f);
        scene.addLight(keyLight);

        return scene;
    }

    @Override
    public void update(float time) {
    }

    @Override
    public float getWalkSpeed() {
        return 3.0f;
    }
}
