package org.example.engine.mesh;

import org.example.engine.math.Vector3;

public final class SphereGenerator {
    private static final int[] VERTEX_PAIRS = {
            0, 1, 0, 2, 0, 3, 0, 4,
            1, 2, 2, 3, 3, 4, 4, 1,
            5, 1, 5, 2, 5, 3, 5, 4
    };

    private static final int[] EDGE_TRIPLETS = {
            0, 1, 4, 1, 2, 5, 2, 3, 6, 3, 0, 7,
            8, 9, 4, 9, 10, 5, 10, 11, 6, 11, 8, 7
    };

    private static final Vector3[] BASE_VERTICES = {
            new Vector3(0.0f, 1.0f, 0.0f),
            new Vector3(-1.0f, 0.0f, 0.0f),
            new Vector3(0.0f, 0.0f, -1.0f),
            new Vector3(1.0f, 0.0f, 0.0f),
            new Vector3(0.0f, 0.0f, 1.0f),
            new Vector3(0.0f, -1.0f, 0.0f)
    };

    private SphereGenerator() {
    }

    public static Mesh generateSphereMesh(int resolution) {
        int numDivisions = Math.max(0, resolution);
        int numVertsPerFace = ((numDivisions + 3) * (numDivisions + 3) - (numDivisions + 3)) / 2;
        int numVerts = numVertsPerFace * 8 - (numDivisions + 2) * 12 + 6;
        int numTrisPerFace = (numDivisions + 1) * (numDivisions + 1);

        VectorList vertices = new VectorList(numVerts);
        IntList triangles = new IntList(numTrisPerFace * 8 * 3);

        for (Vector3 baseVertex : BASE_VERTICES) {
            vertices.add(baseVertex);
        }

        Edge[] edges = new Edge[12];
        for (int i = 0; i < VERTEX_PAIRS.length; i += 2) {
            Vector3 startVertex = vertices.items[VERTEX_PAIRS[i]];
            Vector3 endVertex = vertices.items[VERTEX_PAIRS[i + 1]];

            int[] edgeVertexIndices = new int[numDivisions + 2];
            edgeVertexIndices[0] = VERTEX_PAIRS[i];

            for (int divisionIndex = 0; divisionIndex < numDivisions; divisionIndex++) {
                float t = (divisionIndex + 1.0f) / (numDivisions + 1.0f);
                edgeVertexIndices[divisionIndex + 1] = vertices.nextIndex;
                vertices.add(slerp(startVertex, endVertex, t));
            }

            edgeVertexIndices[numDivisions + 1] = VERTEX_PAIRS[i + 1];
            edges[i / 2] = new Edge(edgeVertexIndices);
        }

        for (int i = 0; i < EDGE_TRIPLETS.length; i += 3) {
            boolean reverse = i / 3 >= 4;
            createFace(
                    edges[EDGE_TRIPLETS[i]],
                    edges[EDGE_TRIPLETS[i + 1]],
                    edges[EDGE_TRIPLETS[i + 2]],
                    reverse,
                    numDivisions,
                    numVertsPerFace,
                    vertices,
                    triangles
            );
        }

        float[] positions = new float[vertices.nextIndex * 3];
        float[] normals = new float[vertices.nextIndex * 3];
        float[] uvs = new float[vertices.nextIndex * 2];

        for (int i = 0; i < vertices.nextIndex; i++) {
            Vector3 v = vertices.items[i].unit_vector();
            positions[i * 3] = v.x;
            positions[i * 3 + 1] = v.y;
            positions[i * 3 + 2] = v.z;

            normals[i * 3] = v.x;
            normals[i * 3 + 1] = v.y;
            normals[i * 3 + 2] = v.z;

            uvs[i * 2] = 0.5f + (float) Math.atan2(v.z, v.x) / ((float) Math.PI * 2.0f);
            uvs[i * 2 + 1] = 0.5f - (float) Math.asin(v.y) / (float) Math.PI;
        }

        int[] indices = new int[triangles.nextIndex];
        System.arraycopy(triangles.items, 0, indices, 0, triangles.nextIndex);

        SubMesh subMesh = new SubMesh("particle_sphere");
        subMesh.setGeometry(positions, normals, uvs, indices, null, null, -1);

        Mesh mesh = new Mesh();
        mesh.addSubMesh(subMesh);
        return mesh;
    }

    private static void createFace(
            Edge sideA,
            Edge sideB,
            Edge bottom,
            boolean reverse,
            int numDivisions,
            int numVertsPerFace,
            VectorList vertices,
            IntList triangles
    ) {
        int numPointsInEdge = sideA.vertexIndices.length;
        IntList vertexMap = new IntList(numVertsPerFace);
        vertexMap.add(sideA.vertexIndices[0]);

        for (int i = 1; i < numPointsInEdge - 1; i++) {
            vertexMap.add(sideA.vertexIndices[i]);

            Vector3 sideAVertex = vertices.items[sideA.vertexIndices[i]];
            Vector3 sideBVertex = vertices.items[sideB.vertexIndices[i]];
            int numInnerPoints = i - 1;

            for (int j = 0; j < numInnerPoints; j++) {
                float t = (j + 1.0f) / (numInnerPoints + 1.0f);
                vertexMap.add(vertices.nextIndex);
                vertices.add(slerp(sideAVertex, sideBVertex, t));
            }

            vertexMap.add(sideB.vertexIndices[i]);
        }

        for (int index : bottom.vertexIndices) {
            vertexMap.add(index);
        }

        int numRows = numDivisions + 1;
        for (int row = 0; row < numRows; row++) {
            int topVertex = ((row + 1) * (row + 1) - row - 1) / 2;
            int bottomVertex = ((row + 2) * (row + 2) - row - 2) / 2;
            int numTrianglesInRow = 1 + 2 * row;

            for (int column = 0; column < numTrianglesInRow; column++) {
                int v0;
                int v1;
                int v2;

                if (column % 2 == 0) {
                    v0 = topVertex;
                    v1 = bottomVertex + 1;
                    v2 = bottomVertex;
                    topVertex++;
                    bottomVertex++;
                } else {
                    v0 = topVertex;
                    v1 = bottomVertex;
                    v2 = topVertex - 1;
                }

                triangles.add(vertexMap.items[v0]);
                triangles.add(vertexMap.items[reverse ? v2 : v1]);
                triangles.add(vertexMap.items[reverse ? v1 : v2]);
            }
        }
    }

    private static Vector3 slerp(Vector3 a, Vector3 b, float t) {
        Vector3 from = a.unit_vector();
        Vector3 to = b.unit_vector();
        float dot = Math.max(-1.0f, Math.min(1.0f, Vector3.dot(from, to)));
        float theta = (float) Math.acos(dot) * t;
        Vector3 relative = to.sub(from.mult(dot)).unit_vector();
        return from.mult((float) Math.cos(theta)).add(relative.mult((float) Math.sin(theta)));
    }

    private static final class Edge {
        final int[] vertexIndices;

        Edge(int[] vertexIndices) {
            this.vertexIndices = vertexIndices;
        }
    }

    private static final class VectorList {
        final Vector3[] items;
        int nextIndex;

        VectorList(int size) {
            items = new Vector3[size];
        }

        void add(Vector3 item) {
            items[nextIndex] = item;
            nextIndex++;
        }
    }

    private static final class IntList {
        final int[] items;
        int nextIndex;

        IntList(int size) {
            items = new int[size];
        }

        void add(int item) {
            items[nextIndex] = item;
            nextIndex++;
        }
    }
}