package org.example.engine.component;

import org.example.engine.mesh.Mesh;

public class MeshFilter extends Component {

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
