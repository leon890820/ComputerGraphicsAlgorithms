package org.example.engine.mesh;

import org.example.engine.math.Vector3;

public interface TriangleVectorGetter {
    Vector3 get(Triangle tri, int vertexIndex);
}
