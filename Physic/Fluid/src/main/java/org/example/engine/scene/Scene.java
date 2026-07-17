package org.example.engine.scene;

import org.example.engine.gameobject.GameObject;
import org.example.engine.light.Light;
import org.example.engine.resource.ResourceDisposalContext;

import java.util.ArrayList;

public class Scene {

    ArrayList<GameObject> objects = new ArrayList<>();
    ArrayList<Light> lights = new ArrayList<>();

    Camera camera;
    RenderMode renderMode = RenderMode.STANDARD;

    public enum RenderMode {
        STANDARD,
        COMPUTE_EXAMPLE,
        PARTICLE_EXAMPLE
    }

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

    public Scene setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode == null ? RenderMode.STANDARD : renderMode;
        return this;
    }

    public RenderMode getRenderMode() {
        return renderMode;
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
        renderMode = RenderMode.STANDARD;
    }
}