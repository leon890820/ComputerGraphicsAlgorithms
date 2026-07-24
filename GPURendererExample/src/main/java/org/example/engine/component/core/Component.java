package org.example.engine.component.core;

import org.example.engine.gameobject.GameObject;
import org.example.engine.render.RenderContext;
import org.example.engine.scene.Transform;

public abstract class Component {
    protected GameObject gameObject;
    private boolean started;
    private boolean enabled = true;

    public GameObject getGameObject() {
        return gameObject;
    }

    public Transform getTransform() {
        return gameObject == null ? null : gameObject.transform;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Component setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public final void attach(GameObject gameObject) {
        this.gameObject = gameObject;
        onAttach();
    }

    public final void startIfNeeded() {
        if (started || !enabled) {
            return;
        }

        started = true;
        start();
    }

    protected void onAttach() {
    }

    public void start() {
    }

    public void update(float deltaTime) {
    }

    public void render(RenderContext ctx) {
    }

    public boolean isRenderedByDefaultPipeline() {
        return false;
    }

    public void dispose() {
    }
}
