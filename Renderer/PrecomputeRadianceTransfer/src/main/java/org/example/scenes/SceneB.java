package org.example.scenes;

import org.example.engine.gameobject.PRTObject;
import org.example.engine.material.PRTMaterial;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneB implements IScene {
    private static final String BUDDHA_MESH = "/meshes/Buddha/buddha";

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);

        PRTObject buddha = new PRTObject(BUDDHA_MESH, new PRTMaterial(), 3, 512);
        buddha.setScale(1.0f, 1.0f, 1.0f)
                .setPosition(0.0f, 0.0f, 0.0f)
                .setEular(0.0f, 3.14f, 0.0f);

        scene.addObject(buddha);

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
