package org.example.scenes;

import org.example.engine.gameobject.MeshObject;
import org.example.engine.light.DirectionalLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneD implements IScene {
    private DirectionalLight keyLight;

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

        keyLight = new DirectionalLight(
                new Vector3(0, 1000, 1000),
                new Vector3(-0.35f, -0.75f, -0.55f),
                new Vector3(0.8f, 0.8f, 0.8f)
        );
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
