package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.light.Light;
import org.example.engine.mesh.Mesh;
import org.example.engine.component.MeshFilter;
import org.example.engine.component.MeshRenderer;
import org.example.engine.mesh.SubMesh;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.component.Animator;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;
import org.example.engine.scene.Transform;

import java.util.ArrayList;

public abstract class GameObject {

    String name;
    public Transform transform;

    MeshFilter meshFilter;
    ArrayList<MeshRenderer> meshRenderers;
    Animator animator;

    public GameObject() {
        transform = new Transform();
        meshRenderers = new ArrayList<>();
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
        if (meshFilter == null) {
            meshFilter = new MeshFilter(m);
        } else {
            meshFilter.setMesh(m);
        }
        return this;
    }

    public MeshFilter getMeshFilter() {
        return meshFilter;
    }

    public GameObject setAnimator(Animator animator) {
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
        for (MeshRenderer renderer : meshRenderers) {
            if (renderer == null) {
                continue;
            }

            if (disposalContext != null) {
                disposalContext.trackMaterial(renderer.getMaterial());

                SubMesh subMesh = renderer.getSubMesh();
                if (subMesh != null) {
                    disposalContext.trackTexture(subMesh.textureKa);
                }
            }

            renderer.dispose();
        }

        meshRenderers.clear();
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

    public void run() {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.render(createMaterialRenderData(null, mr));
            }
        }
    }

    public void run(RenderContext ctx) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.render(createMaterialRenderData(ctx, mr));
            }
        }
    }

    public void runWithMaterial(Material overrideMaterial) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.render(createMaterialRenderData(null, mr), overrideMaterial);
            }
        }
    }

    public void runWithMaterial(RenderContext ctx, Material overrideMaterial) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.render(createMaterialRenderData(ctx, mr), overrideMaterial);
            }
        }
    }

    public void debugRun() {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.debugRender(createMaterialRenderData(null, mr));
            }
        }
    }

    public void debugRun(RenderContext ctx) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.debugRender(createMaterialRenderData(ctx, mr));
            }
        }
    }

    public void debugRunWithMaterial(Material overrideMaterial) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.debugRender(createMaterialRenderData(null, mr), overrideMaterial);
            }
        }
    }

    public void debugRunWithMaterial(RenderContext ctx, Material overrideMaterial) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.debugRender(createMaterialRenderData(ctx, mr), overrideMaterial);
            }
        }
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
            meshRenderers.add(mr);
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

    protected MaterialRenderData createMaterialRenderData(
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
            data.mvpMatrix = ctx.camera.Matrix().mult(data.modelMatrix);
        }

        if (subMesh != null) {
            data.baseColorTexture = subMesh.textureKa;
        }

        return data;
    }
}

