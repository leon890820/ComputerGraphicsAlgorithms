package org.example.engine.scene;

import org.example.engine.gameobject.GameObject;
import org.example.engine.light.Light;

import java.util.ArrayList;

public class Scene {

    ArrayList<GameObject> objects = new ArrayList<>();
    ArrayList<Light> lights = new ArrayList<>();

    Camera camera;

    public Scene setCamera(Camera cam) {
        this.camera = cam;
        return this;
    }

    public Scene addObject(GameObject go) {
        if (go != null) {
            objects.add(go);
        }
        return this;
    }

    public Scene addLight(Light light) {
        if (light != null) {
            lights.add(light);
        }
        return this;
    }

    public ArrayList<GameObject> getObjects() {
        return objects;
    }

    public ArrayList<Light> getLights() {
        return lights;
    }

    public Camera getCamera() {
        return camera;
    }

    // ===== optional（之後會用到）=====

    public void clear() {
        objects.clear();
        lights.clear();
        camera = null;
    }
}