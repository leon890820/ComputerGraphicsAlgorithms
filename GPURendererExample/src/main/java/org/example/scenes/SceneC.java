package org.example.scenes;

import org.example.engine.gameobject.MeshObject;
import org.example.engine.gl.Texture;
import org.example.engine.light.PointLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneC implements IScene {
    private PointLight light;
    private MeshObject gura;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        camera.transform.setPosition(0, 1.0f, 3.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        light = new PointLight(
                new Vector3(4, 6, 4),
                new Vector3(0.9f, 0.9f, 0.9f)
        );

        PhongMaterial guraMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        gura = new MeshObject("../Model/GuraAnim/gura.glb", guraMaterial);
        gura.playAnimation("smolguraAnimationsRESOURCE");
        gura.setScale(1f, 1f, 1f)
                .setPosition(0, 0, 0);

        PhongMaterial floorMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");
        floorMaterial.setTexture(new Texture("/textures/Floor.png"));

        MeshObject floor = new MeshObject("/meshes/quad", floorMaterial);
        floor.setEular(3.1415926f / 2, 0, 0)
                .setScale(5, 5, 5)
                .setPosition(0, -0.05f, 0);

        gura.setScene(scene);
        floor.setScene(scene);

        scene.addObject(gura);
        scene.addObject(floor);
        scene.addLight(light);

        return scene;
    }

    @Override
    public void update(float time) {
//        if (light != null) {
//            light.setPosition(
//                    (float) Math.cos(time) * 4,
//                    6f,
//                    (float) Math.sin(time) * 4
//            );
//        }

        if (gura != null) {
            gura.updateAnimation(time);
        }
    }

    @Override
    public float getWalkSpeed() {
        return 0.05f;
    }
}
