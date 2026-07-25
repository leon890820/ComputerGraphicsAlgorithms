package org.example.engine.raytracing;

import org.example.engine.math.Vector3;

import java.util.ArrayList;

public class CornellBoxFactory {
    public CornellBox createOpenFrontBox(float boxSize) {
        return createOpenFrontBox(boxSize, CornellBoxMaterials.defaultMaterials());
    }

    public CornellBox createOpenFrontBox(float boxSize, CornellBoxMaterials materials) {
        CornellBox box = new CornellBox();
        CornellBoxMaterials safeMaterials = materials == null ? CornellBoxMaterials.defaultMaterials() : materials;

        addQuad(
                box,
                new Vector3(-0.35f * boxSize, boxSize - 0.01f, -0.35f * boxSize),
                new Vector3(0.35f * boxSize, boxSize - 0.01f, -0.35f * boxSize),
                new Vector3(0.35f * boxSize, boxSize - 0.01f, 0.35f * boxSize),
                new Vector3(-0.35f * boxSize, boxSize - 0.01f, 0.35f * boxSize),
                safeMaterials.light
        );

        addQuad(
                box,
                new Vector3(boxSize, -boxSize, boxSize),
                new Vector3(-boxSize, -boxSize, boxSize),
                new Vector3(-boxSize, boxSize, boxSize),
                new Vector3(boxSize, boxSize, boxSize),
                safeMaterials.rightWall
        );
        addQuad(
                box,
                new Vector3(-boxSize, -boxSize, -boxSize),
                new Vector3(boxSize, -boxSize, -boxSize),
                new Vector3(boxSize, boxSize, -boxSize),
                new Vector3(-boxSize, boxSize, -boxSize),
                safeMaterials.leftWall
        );
        addQuad(
                box,
                new Vector3(boxSize, boxSize, -boxSize),
                new Vector3(-boxSize, boxSize, -boxSize),
                new Vector3(-boxSize, boxSize, boxSize),
                new Vector3(boxSize, boxSize, boxSize),
                safeMaterials.white
        );
        addQuad(
                box,
                new Vector3(boxSize, -boxSize, -boxSize),
                new Vector3(boxSize, -boxSize, boxSize),
                new Vector3(-boxSize, -boxSize, boxSize),
                new Vector3(-boxSize, -boxSize, -boxSize),
                safeMaterials.white
        );
        addQuad(
                box,
                new Vector3(-boxSize, -boxSize, -boxSize),
                new Vector3(-boxSize, -boxSize, boxSize),
                new Vector3(-boxSize, boxSize, boxSize),
                new Vector3(-boxSize, boxSize, -boxSize),
                safeMaterials.white
        );

        return box;
    }

    private void addQuad(CornellBox box, Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3, RayTracingMaterialData material) {
        addTriangle(box, p0, p1, p2, material);
        addTriangle(box, p0, p2, p3, material);
    }

    private void addTriangle(CornellBox box, Vector3 p0, Vector3 p1, Vector3 p2, RayTracingMaterialData material) {
        box.triangles.add(new RayTracingTriangle(p0, p1, p2));
        box.materials.add(material);
    }

    public static class CornellBox {
        public final ArrayList<RayTracingTriangle> triangles = new ArrayList<>();
        public final ArrayList<RayTracingMaterialData> materials = new ArrayList<>();
    }

    public static class CornellBoxMaterials {
        public final RayTracingMaterialData light;
        public final RayTracingMaterialData rightWall;
        public final RayTracingMaterialData leftWall;
        public final RayTracingMaterialData white;

        public CornellBoxMaterials(
                RayTracingMaterialData light,
                RayTracingMaterialData rightWall,
                RayTracingMaterialData leftWall,
                RayTracingMaterialData white
        ) {
            this.light = light == null ? RayTracingMaterialData.emissive(new Vector3(5.0f)) : light;
            this.rightWall = rightWall == null ? RayTracingMaterialData.metal(new Vector3(1.0f, 0.0f, 0.0f), 0.9f) : rightWall;
            this.leftWall = leftWall == null ? RayTracingMaterialData.metal(new Vector3(0.0f, 1.0f, 0.0f), 0.9f) : leftWall;
            this.white = white == null ? RayTracingMaterialData.metal(new Vector3(1.0f), 0.9f) : white;
        }

        public static CornellBoxMaterials defaultMaterials() {
            return new CornellBoxMaterials(
                    RayTracingMaterialData.emissive(new Vector3(5.0f)),
                    RayTracingMaterialData.metal(new Vector3(1.0f, 0.0f, 0.0f), 0.9f),
                    RayTracingMaterialData.metal(new Vector3(0.0f, 1.0f, 0.0f), 0.9f),
                    RayTracingMaterialData.metal(new Vector3(1.0f), 0.9f)
            );
        }
    }
}
