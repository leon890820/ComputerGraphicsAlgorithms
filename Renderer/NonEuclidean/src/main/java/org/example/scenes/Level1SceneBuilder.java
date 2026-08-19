package org.example.scenes;

import org.example.engine.gameobject.MeshObject;
import org.example.engine.gl.Texture;
import org.example.engine.light.DirectionalLight;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.portal.Portal;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

final class Level1SceneBuilder {
    private static final float PLAYER_HEIGHT = 1.5f;

    enum ViewPreset {
        PLAYER_START,
        LEFT_TUNNEL,
        OVERVIEW
    }

    private Level1SceneBuilder() {
    }

    static Scene load(Camera camera, int screenWidth, int screenHeight, ViewPreset preset) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.01f, 100.0f);
        applyCameraPreset(camera, preset);

        Scene scene = new Scene();
        scene.setCamera(camera);

        DirectionalLight light = new DirectionalLight(
                new Vector3(0, 8, 4),
                new Vector3(-0.35f, -1.0f, -0.25f),
                new Vector3(0.95f, 0.92f, 0.86f)
        );
        light.setOrtho(-12, 12, -12, 12, 0.1f, 40.0f);

        PhongMaterial tunnelMaterial = makeMaterial("/textures/checker_gray.bmp");
        PhongMaterial groundMaterial = makeMaterial("/textures/checker_green.bmp");

        MeshObject longTunnel = new MeshObject("/meshes/tunnel", tunnelMaterial);
        longTunnel.setPosition(-2.4f, 0.0f, -1.8f)
                .setScale(1.0f, 1.0f, 4.8f);

        MeshObject shortTunnel = new MeshObject("/meshes/tunnel", tunnelMaterial);
        shortTunnel.setPosition(2.4f, 0.0f, 0.0f)
                .setScale(1.0f, 1.0f, 0.6f);

        MeshObject ground = new MeshObject("/meshes/ground", groundMaterial);
        ground.setScale(12.0f, 1.0f, 12.0f);

        scene.addObject(longTunnel);
        scene.addObject(shortTunnel);
        scene.addObject(ground);
        addLevel1Portals(scene);
        scene.addLight(light);

        return scene;
    }

    private static PhongMaterial makeMaterial(String texturePath) {
        PhongMaterial material =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");
        material.setTexture(new Texture(texturePath));
        return material;
    }

    private static void addLevel1Portals(Scene scene) {
        Portal portal1 = createPortal(-2.4f, 1.0f, 3.0f, 0.6f, 0.999f);
        Portal portal2 = createPortal(2.4f, 1.0f, 0.6f, 0.6f, 0.999f);
        Portal portal3 = createPortal(-2.4f, 1.0f, -6.6f, 0.6f, 0.999f);
        Portal portal4 = createPortal(2.4f, 1.0f, -0.6f, 0.6f, 0.999f);

        Portal.connect(portal1, portal2);
        Portal.connect(portal3, portal4);

        scene.addPortal(portal1);
        scene.addPortal(portal2);
        scene.addPortal(portal3);
        scene.addPortal(portal4);
    }

    private static Portal createPortal(float x, float y, float z, float sx, float sy) {
        Portal portal = new Portal();
        portal.setPosition(x, y, z)
                .setScale(sx, sy, 1.0f);
        return portal;
    }

    private static void applyCameraPreset(Camera camera, ViewPreset preset) {
        if (preset == ViewPreset.LEFT_TUNNEL) {
            camera.setPositionOrientation(
                    new Vector3(-2.4f, PLAYER_HEIGHT, 3.8f),
                    0.0f,
                    0.0f
            );
            return;
        }

        if (preset == ViewPreset.OVERVIEW) {
            camera.setPositionOrientation(
                    new Vector3(0.0f, 6.0f, 8.0f),
                    -0.55f,
                    0.0f
            );
            return;
        }

        camera.setPositionOrientation(
                new Vector3(0.0f, PLAYER_HEIGHT, 5.0f),
                0.0f,
                0.0f
        );
    }
}
