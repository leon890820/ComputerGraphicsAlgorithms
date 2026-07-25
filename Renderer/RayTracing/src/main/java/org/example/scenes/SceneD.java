package org.example.scenes;

import org.example.engine.component.render.RayTracingDisplay;
import org.example.engine.gameobject.EmptyObject;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.raytracing.RayTracingMaterialData;
import org.example.engine.raytracing.RayTracingMeshInstance;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

import java.util.ArrayList;

public class SceneD implements IScene {
    private static final String DRAGON_MESH_PATH = "C:/Users/USER/Desktop/code/Java/ComputerGraphicsAlgorithms/Model/Dragon/dragon.obj";
    private static final String SPONZA_MESH_PATH = "C:/Users/USER/Desktop/code/Java/ComputerGraphicsAlgorithms/Model/sponza/sponza.obj";
    private static final float DRAGON_TARGET_SIZE = 1.8f;


    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 75.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        camera.setPositionOrientation(new org.example.engine.math.Vector3(0.0f, -3.0f, 3.0f), 0.0f, 0f);

        Scene scene = new Scene();
        scene.setCamera(camera);
        EmptyObject display = new EmptyObject();
        display.addComponent(new RayTracingDisplay(screenWidth, screenHeight, createStaticModels()));
        display.setEular(0,1.57f,0);
        scene.addObject(display);
        return scene;
    }

    private ArrayList<RayTracingMeshInstance> createStaticModels() {
        ArrayList<RayTracingMeshInstance> models = new ArrayList<>();
        models.add(new RayTracingMeshInstance(
                SPONZA_MESH_PATH,
                Matrix4.Trans(new Vector3(0.0f, 0, 0.0f))
                        .mult(Matrix4.Scale(2f)),
                RayTracingMaterialData.metal(new Vector3(0.6f, 0.6f, 0.58f), 0.9f)
        ));
        models.add(RayTracingMeshInstance.normalized(
                DRAGON_MESH_PATH,
                Matrix4.Trans(new Vector3(0.0f, -3.7f, 0.0f)).mult(Matrix4.RotY(-1.57f)),
                RayTracingMaterialData.dielectric(new Vector3(0.5f), 0.8f, 1.5f),
                DRAGON_TARGET_SIZE
        ));
        models.add(RayTracingMeshInstance.normalized(
                DRAGON_MESH_PATH,
                Matrix4.Trans(new Vector3(0.6f, -4.2f, 0.5f))
                        .mult(Matrix4.RotY(-2.0f))
                        .mult(Matrix4.Scale(0.35f)),
                RayTracingMaterialData.metal(new Vector3(0.9f, 0.75f, 0.45f), 0.18f),
                DRAGON_TARGET_SIZE
        ));

        models.add(RayTracingMeshInstance.normalized(
                DRAGON_MESH_PATH,
                Matrix4.Trans(new Vector3(0.0f, -4.1f, 0.8f))
                        .mult(Matrix4.RotY(2.0f))
                        .mult(Matrix4.Scale(0.5f)),
                RayTracingMaterialData.lambertian(new Vector3(0.3f, 0.75f, 0.45f)),
                DRAGON_TARGET_SIZE
        ));
        return models;
    }

    @Override
    public void update(float time) {
    }

    @Override
    public float getWalkSpeed() {
        return 0.03f;
    }
}
