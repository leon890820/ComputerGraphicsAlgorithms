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
    private MeshObject ame;
    private MeshObject calli;
    private MeshObject ina;
    private MeshObject rushia;

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

        gura = new MeshObject("../Model/Hololive/gura.glb", guraMaterial);
        gura.playAnimation("smolguraAnimationsRESOURCE");
        gura.setScale(1f, 1f, 1f)
                .setPosition(-1, 0, 0);

        ame = new MeshObject("../Model/Hololive/ame.glb", guraMaterial);
        ame.playAnimation("smolameAnimationsRESOURCE");
        ame.setPosition(1f, 0f, 0f).setScale(3f, 3f, 3f);

        calli = new MeshObject("../Model/Hololive/calli.glb", guraMaterial);
        calli.playAnimation("smolcalliAnimationsRESOURCE");
        calli.setPosition(3f, 0f, 0f).setScale(1f, 1f, 1f);

        ina = new MeshObject("../Model/Hololive/ina.glb", guraMaterial);
        ina.playAnimation("smolinaAnimationsRESOURCE");
        ina.setPosition(-3f, 0f, 0f).setScale(3f, 3f, 3f);

//        rushia = new MeshObject("../Model/Rushia/rushia.glb", guraMaterial);
//        rushia.playAnimation("idle");
//        rushia.setPosition(2,0,0).setScale(0.5f,0.5f,0.5f);

        PhongMaterial floorMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");
        floorMaterial.setTexture(new Texture("/textures/Floor.png"));

        MeshObject floor = new MeshObject("/meshes/quad", floorMaterial);
        floor.setEular(3.1415926f / 2, 0, 0)
                .setScale(10, 10, 10)
                .setPosition(0, -0.05f, 0);

        scene.addObject(gura);
        scene.addObject(ame);
        scene.addObject(calli);
        scene.addObject(ina);
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

        gura.updateAnimation(time);
        ina.updateAnimation(time);
        calli.updateAnimation(time);
        ame.updateAnimation(time);
    }

    @Override
    public float getWalkSpeed() {
        return 0.05f;
    }
}
