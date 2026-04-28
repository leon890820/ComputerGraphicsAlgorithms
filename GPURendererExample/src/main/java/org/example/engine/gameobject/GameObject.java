package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.MeshFilter;
import org.example.engine.mesh.MeshRenderer;
import org.example.engine.mesh.SubMesh;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Scene;
import org.example.engine.scene.Transform;

import java.util.ArrayList;

public abstract class GameObject {

    String name;
    public Transform transform;

    MeshFilter meshFilter;
    ArrayList<MeshRenderer> meshRenderers;

    // ⭐ 改掉 Processing global（你之後可以改成 Scene 管）
    public Scene scene;

    public GameObject() {
        transform = new Transform();
        meshRenderers = new ArrayList<>();
    }

    public  GameObject setScene(Scene scene){
        this.scene = scene;
        return this;
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

    public ArrayList<MeshRenderer> getMeshRenderers() {
        return meshRenderers;
    }

    Vector3 getPosition() {
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
                mr.render(this);
            }
        }
    }

    public void runWithMaterial(Material overrideMaterial) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.render(this, overrideMaterial);
            }
        }
    }

    public void debugRun() {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.debugRender(this);
            }
        }
    }

    public void debugRunWithMaterial(Material overrideMaterial) {
        for (MeshRenderer mr : meshRenderers) {
            if (mr != null) {
                mr.debugRender(this, overrideMaterial);
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

    public Matrix4 MVP() {
        var main_camera = scene.getCamera();
        return main_camera.Matrix().mult(localToWorld());
    }

    public void buildSubMeshRenderers(Material defaultMaterial) {
        clearMeshRenderers();

        if (meshFilter == null) {
            System.out.println("[GameObject] buildSubMeshRenderers failed: meshFilter is null");
            return;
        }

        Mesh mesh = meshFilter.getMesh();
        ArrayList<SubMesh> subs = mesh.getAllSubMeshes();

        for (SubMesh sub : subs) {
            MeshRenderer mr = new MeshRenderer(sub, defaultMaterial);
            mr.initialize();
            meshRenderers.add(mr);
        }

        System.out.println("[GameObject] buildSubMeshRenderers success: " + meshRenderers.size());
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
}