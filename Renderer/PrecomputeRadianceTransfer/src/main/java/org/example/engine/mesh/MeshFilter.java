package org.example.engine.mesh;

public class MeshFilter {

    private Mesh mesh;

    public MeshFilter(Mesh m) {
        this.mesh = m;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh m) {
        this.mesh = m;
    }

    public boolean hasMesh() {
        return mesh != null;
    }

    public void clear() {
        mesh = null;
    }
}
