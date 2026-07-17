package org.example.engine.scene;

import org.example.engine.gameobject.GameObject;
import org.example.engine.light.Light;
import org.example.engine.resource.ResourceDisposalContext;

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


    public void clear() {
        ResourceDisposalContext disposalContext = new ResourceDisposalContext();

        for (GameObject object : objects) {
            if (object != null) {
                object.dispose(disposalContext);
            }
        }

        disposalContext.disposeAll();
        objects.clear();
        lights.clear();
        camera = null;
    }
}