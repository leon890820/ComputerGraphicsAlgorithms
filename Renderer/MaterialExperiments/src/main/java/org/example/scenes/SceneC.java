package org.example.scenes;

import org.example.engine.gameobject.MeshObject;
import org.example.engine.gl.Texture;
import org.example.engine.light.PointLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneC implements IScene {
    private static final String SPHERE_MODEL = "../../Model/Shape/sphere.glb";
    private static final String BOX_MODEL = "../../Model/Shape/portal_companion_cube.glb";

    private PointLight light;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        camera.transform.setPosition(0, 2.0f, 7.0f);
        camera.setEular(-0.25f, 0.0f, 0.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        light = new PointLight(
                new Vector3(2.5f, 6.0f, 3.5f),
                new Vector3(1.0f, 0.95f, 0.85f)
        ).setNearFar(0.1f, 40.0f);

        PhongMaterial shapeMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");
        shapeMaterial.setBaseColor(0.55f, 0.55f, 0.55f);

        PhongMaterial floorMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");
        floorMaterial.setTexture(new Texture("/textures/Floor.png"));

        MeshObject floor = new MeshObject("/meshes/quad", floorMaterial);
        floor.setEular(-3.1415926f / 2, 0, 0)
                .setScale(12, 12, 12)
                .setPosition(0, -0.05f, 0);

        MeshObject sphereA = new MeshObject(SPHERE_MODEL, shapeMaterial);
        sphereA.setScale(0.8f, 0.8f, 0.8f)
                .setPosition(-2.5f, 0.635f, -1.0f);

        MeshObject sphereB = new MeshObject(SPHERE_MODEL, shapeMaterial);
        sphereB.setScale(0.55f, 0.55f, 0.55f)
                .setPosition(1.8f, 0.421f, 0.8f);

        MeshObject boxA = new MeshObject(BOX_MODEL, shapeMaterial);
        boxA.setEular(0.0f, 0.45f, 0.0f)
                .setScale(0.003f, 0.003f, 0.003f)
                .setPosition(-0.2f, 0.79f, 0.2f);

        MeshObject boxB = new MeshObject(BOX_MODEL, shapeMaterial);
        boxB.setEular(0.0f, -0.7f, 0.0f)
                .setScale(0.0022f, 0.0022f, 0.0022f)
                .setPosition(3.0f, 0.565f, -1.6f);

        MeshObject boxC = new MeshObject(BOX_MODEL, shapeMaterial);
        boxC.setEular(0.0f, 0.25f, 0.0f)
                .setScale(0.0018f, 0.0018f, 0.0018f)
                .setPosition(-3.5f, 0.453f, 1.7f);

        scene.addObject(floor);
        scene.addObject(sphereA);
        scene.addObject(sphereB);
        scene.addObject(boxA);
        scene.addObject(boxB);
        scene.addObject(boxC);
        scene.addLight(light);

        return scene;
    }

    @Override
    public void update(float time) {
    }

    @Override
    public float getWalkSpeed() {
        return 0.05f;
    }
}



