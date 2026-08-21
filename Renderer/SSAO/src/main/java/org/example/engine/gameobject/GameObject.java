package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.light.Light;
import org.example.engine.mesh.Mesh;
import org.example.engine.component.core.Component;
import org.example.engine.component.render.MeshFilter;
import org.example.engine.component.render.MeshRenderer;
import org.example.engine.mesh.SubMesh;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.component.core.Animator;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;
import org.example.engine.scene.Transform;

import java.util.ArrayList;

public abstract class GameObject {

    String name;
    public Transform transform;

    MeshFilter meshFilter;
    ArrayList<MeshRenderer> meshRenderers;
    ArrayList<Component> components;
    Animator animator;

    public GameObject() {
        transform = new Transform();
        meshRenderers = new ArrayList<>();
        components = new ArrayList<>();
    }

    public GameObject setTransform(Transform trans) {
        transform = trans;
        transform.forceDirty();
        return this;
    }

    public GameObject setTransform(Vector3 pos, Vector3 eular, Vector3 scale) {
        transform.setPosition(pos).setEular(eular).setScale(scale);
        return this;
    }

    public GameObject setPosition(Vector3 pos) {
        transform.setPosition(pos);
        return this;
    }

    public GameObject setEular(Vector3 eular) {
        transform.setEular(eular);
        return this;
    }

    public GameObject setScale(Vector3 scale) {
        transform.setScale(scale);
        return this;
    }

    public GameObject setPosition(float x, float y, float z) {
        transform.setPosition(x, y, z);
        return this;
    }

    public GameObject setEular(float x, float y, float z) {
        transform.setEular(x, y, z);
        return this;
    }

    public GameObject setScale(float x, float y, float z) {
        transform.setScale(x, y, z);
        return this;
    }

    public GameObject setMesh(Mesh m) {
        MeshFilter filter = getComponent(MeshFilter.class);

        if (filter == null) {
            filter = addComponent(new MeshFilter(m));
        } else {
            filter.setMesh(m);
        }

        meshFilter = filter;
        return this;
    }

    public MeshFilter getMeshFilter() {
        return meshFilter;
    }

    public GameObject setAnimator(Animator animator) {
        if (animator != null && animator.getGameObject() != this) {
            addComponent(animator);
        }
        this.animator = animator;
        return this;
    }

    public Animator getAnimator() {
        return animator;
    }

    public boolean hasAnimator() {
        return animator != null;
    }

    public void playAnimation(String animationName) {
        if (animator != null) {
            animator.play(animationName);
        }
    }

    public void updateAnimation(float time) {
        if (animator != null) {
            animator.updateAbsolute(time);
        }
    }

    public GameObject setName(String s) {
        name = s;
        return this;
    }

    public void clearMeshRenderers() {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.dispose();
                components.remove(mr);
            }
        }
        meshRenderers.clear();
    }

    public void dispose() {
        ResourceDisposalContext disposalContext = new ResourceDisposalContext();
        dispose(disposalContext);
        disposalContext.disposeAll();
    }

    public void dispose(ResourceDisposalContext disposalContext) {
        for (Component component : new ArrayList<>(components)) {
            if (component instanceof MeshRenderer && disposalContext != null) {
                MeshRenderer renderer = (MeshRenderer) component;
                disposalContext.trackMaterial(renderer.getMaterial());

                SubMesh subMesh = renderer.getSubMesh();
                if (subMesh != null) {
                    disposalContext.trackTexture(subMesh.textureKa);
                }
            }

            component.dispose();
        }

        components.clear();
        meshRenderers.clear();
    }

    public <T extends Component> T addComponent(T component) {
        if (component == null) {
            return null;
        }

        if (!components.contains(component)) {
            components.add(component);
            component.attach(this);
        }

        if (component instanceof MeshFilter) {
            meshFilter = (MeshFilter) component;
        }

        if (component instanceof MeshRenderer && !meshRenderers.contains(component)) {
            meshRenderers.add((MeshRenderer) component);
        }

        if (component instanceof Animator) {
            animator = (Animator) component;
        }

        return component;
    }

    public <T extends Component> T getComponent(Class<T> type) {
        if (type == null) {
            return null;
        }

        for (Component component : components) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
        }

        return null;
    }

    public <T extends Component> ArrayList<T> getComponents(Class<T> type) {
        ArrayList<T> result = new ArrayList<>();

        if (type == null) {
            return result;
        }

        for (Component component : components) {
            if (type.isInstance(component)) {
                result.add(type.cast(component));
            }
        }

        return result;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public void startComponentsIfNeeded() {
        for (Component component : components) {
            component.startIfNeeded();
        }
    }

    public void updateComponents(float deltaTime) {
        startComponentsIfNeeded();

        for (Component component : components) {
            if (component.isEnabled()) {
                component.update(deltaTime);
            }
        }
    }

    public void renderComponents(RenderContext ctx) {
        startComponentsIfNeeded();

        for (Component component : components) {
            if (component.isEnabled()) {
                component.render(ctx);
            }
        }
    }

    public void renderDefaultComponents(RenderContext ctx) {
        startComponentsIfNeeded();

        for (Component component : components) {
            if (component.isEnabled() && component.isRenderedByDefaultPipeline()) {
                component.render(ctx);
            }
        }
    }

    public void renderCustomComponents(RenderContext ctx) {
        startComponentsIfNeeded();

        for (Component component : components) {
            if (component.isEnabled() && !component.isRenderedByDefaultPipeline()) {
                component.render(ctx);
            }
        }
    }

    public ArrayList<MeshRenderer> getMeshRenderers() {
        return meshRenderers;
    }

    public Vector3 getPosition() {
        return transform.position;
    }

    Vector3 getEular() {
        return transform.eular;
    }

    Vector3 getScale() {
        return transform.scale;
    }

    public Matrix4 localToWorld() {
        return transform.localToWorld();
    }

    public Matrix4 worldToLocal() {
        return transform.worldToLocal();
    }

    public Vector3 forward() {
        return localToWorld().transformDirection(new Vector3(0, 0, -1)).unit_vector();
    }

    public Vector3 right() {
        return localToWorld().transformDirection(new Vector3(1, 0, 0)).unit_vector();
    }

    public Vector3 up() {
        return localToWorld().transformDirection(new Vector3(0, 1, 0)).unit_vector();
    }

    public Matrix4[] getBoneMatricesForSubMesh(SubMesh subMesh) {
        if (animator == null || subMesh == null || !subMesh.hasSkinWeights()) {
            return null;
        }

        return animator.getBoneMatrices(subMesh.skinIndex);
    }

    public void buildSubMeshRenderers(Material defaultMaterial) {
        clearMeshRenderers();

        if (meshFilter == null) {
            System.out.println("[GameObject] buildSubMeshRenderers failed: meshFilter is null");
            return;
        }

        Mesh mesh = meshFilter.getMesh();
        mesh.finishBuild();
        ArrayList<SubMesh> subs = mesh.getAllSubMeshes();

        for (SubMesh sub : subs) {
            MeshRenderer mr = new MeshRenderer(sub, defaultMaterial);
            mr.initialize();
            addComponent(mr);
        }
    }

    public void setMaterial(Material mat) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.setMaterial(mat);
            }
        }
    }

    public Material getMaterial() {
        if (meshRenderers == null || meshRenderers.size() == 0) return null;
        return meshRenderers.get(0).getMaterial();
    }

    public MaterialRenderData createMaterialRenderData(
            RenderContext ctx,
            MeshRenderer meshRenderer
    ) {
        SubMesh subMesh = meshRenderer == null ? null : meshRenderer.getSubMesh();

        MaterialRenderData data = new MaterialRenderData();
        data.modelMatrix = localToWorld();
        data.boneMatrices = getBoneMatricesForSubMesh(subMesh);

        Light light = ctx == null ? null : ctx.activeLight;
        if (light != null) {
            data.hasLight = true;
            data.lightPosition = light.transform.position;
            data.lightColor = light.getLightColor();
            data.lightDirection = light.getLightDir();
            data.lightFar = light.getLightFar();
            data.lightSpaceMatrix = light.getProjectionMatrix().mult(light.getViewMatrix());
        }

        if (ctx != null && ctx.camera != null) {
            data.viewPosition = ctx.camera.transform.position;
            data.viewMatrix = ctx.camera.getViewMatrix();
            data.mvpMatrix = ctx.camera.Matrix().mult(data.modelMatrix);
        }

        if (subMesh != null) {
            data.baseColorTexture = subMesh.textureKa;
        }

        return data;
    }
}

