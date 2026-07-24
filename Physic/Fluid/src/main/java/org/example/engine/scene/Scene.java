package org.example.engine.scene;

import org.example.engine.gameobject.GameObject;
import org.example.engine.component.core.Component;
import org.example.engine.light.Light;
import org.example.engine.render.RenderContext;
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

    public <T extends Component> ArrayList<T> getComponents(Class<T> type) {
        ArrayList<T> result = new ArrayList<>();

        if (type == null) {
            return result;
        }

        for (GameObject object : objects) {
            if (object != null) {
                result.addAll(object.getComponents(type));
            }
        }

        return result;
    }

    public void update(float deltaTime) {
        for (GameObject object : objects) {
            if (object != null) {
                object.updateComponents(deltaTime);
            }
        }
    }

    public void render(RenderContext ctx) {
        for (GameObject object : objects) {
            if (object != null) {
                object.renderComponents(ctx);
            }
        }
    }

    public void renderDefault(RenderContext ctx) {
        for (GameObject object : objects) {
            if (object != null) {
                object.renderDefaultComponents(ctx);
            }
        }
    }

    public void renderCustom(RenderContext ctx) {
        for (GameObject object : objects) {
            if (object != null) {
                object.renderCustomComponents(ctx);
            }
        }
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
