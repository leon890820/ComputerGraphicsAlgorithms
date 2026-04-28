package org.example;

import org.example.engine.core.Window;
import org.example.engine.gameobject.PhongObject;
import org.example.engine.gl.Texture;
import org.example.engine.material.PhongMaterial;
import org.example.engine.light.*;
import org.example.engine.math.*;
import org.example.engine.render.*;
import org.example.engine.scene.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Main {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    private static final float GH_MOUSE_SENSITIVITY = 0.005f;
    private static final float GH_WALK_SPEED = 0.05f;
    private static final float MOUSE_DEAD_ZONE = 2;

    private final boolean[] key_input = new boolean[4];

    private boolean mouseInitialized = false;

    private double mouseX;
    private double mouseY;
    private double pmouseX;
    private double pmouseY;

    private Window window;
    private Camera main_camera;
    private Scene scene;
    private Light light;
    private Renderer renderer;
    private RenderContext ctx;

    private float a = 0;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        setup();

        while (!window.shouldClose()) {
            draw();
        }

        window.destroy();
    }

    private void setup() {
        window = new Window(WIDTH, HEIGHT, "Processing Port - GBufferPass");
        window.create();

        setupInput(window);

        main_camera = new Camera();
        main_camera.setSize(WIDTH, HEIGHT, 0.1f, 1000.0f);
        main_camera.transform.setPosition(0, 1.0f, 3.0f);

        scene = new Scene();
        scene.setCamera(main_camera);

        light = new PointLight(
                new Vector3(10, 10, 0),
                new Vector3(0.8f, 0.8f, 0.8f)
        );

        renderer = new Renderer(WIDTH, HEIGHT);
        ctx = new RenderContext(scene, main_camera, WIDTH, HEIGHT);

        PhongMaterial phongMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        PhongObject phongObject =
                new PhongObject("/meshes/Furina/Furina", phongMaterial);

        PhongMaterial floorMaterial =
                new PhongMaterial("/shaders/BlinnPhong.frag", "/shaders/BlinnPhong.vert");

        Texture floorTexture = new Texture("/textures/Floor.png");
        floorMaterial.setTexture(floorTexture);

        PhongObject floor = new PhongObject("/meshes/quad", floorMaterial);
        floor.setEular(3.1415926f / 2, 0, 0)
                .setScale(5, 5, 5)
                .setPosition(0, 0, 0);

        phongObject.setScene(scene);
        floor.setScene(scene);

        scene.addObject(phongObject);
        scene.addObject(floor);
        scene.addLight(light);
    }

    private void draw() {
        move(window);

        light.setPosition(
                (float) Math.cos(a) * 10,
                10f,
                (float) Math.sin(a) * 10
        );

        a += 0.02f;

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderer.render(ctx);

        window.swapBuffers();
        window.pollEvents();
    }

    private void setupInput(Window window) {
        long handle = window.getHandle();

        glfwSetKeyCallback(handle, (w, key, scancode, action, mods) -> {
            boolean pressed = action != GLFW_RELEASE;

            if (key == GLFW_KEY_W) {
                key_input[0] = pressed;
            }

            if (key == GLFW_KEY_A) {
                key_input[1] = pressed;
            }

            if (key == GLFW_KEY_S) {
                key_input[2] = pressed;
            }

            if (key == GLFW_KEY_D) {
                key_input[3] = pressed;
            }

            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(handle, true);
            }
        });

        glfwSetCursorPosCallback(handle, (w, x, y) -> {
            if (!mouseInitialized) {
                mouseX = x;
                mouseY = y;
                pmouseX = x;
                pmouseY = y;
                mouseInitialized = true;
                return;
            }

            pmouseX = mouseX;
            pmouseY = mouseY;
            mouseX = x;
            mouseY = y;
        });
    }

    private void move(Window window) {
        long handle = window.getHandle();

        boolean rightMousePressed =
                glfwGetMouseButton(handle, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;

        if (rightMousePressed) {
            float rawDx = (float) (pmouseX - mouseX);
            float rawDy = (float) (pmouseY - mouseY);

            if (Math.abs(rawDx) < MOUSE_DEAD_ZONE) {
                rawDx = 0.0f;
            }

            if (Math.abs(rawDy) < MOUSE_DEAD_ZONE) {
                rawDy = 0.0f;
            }

            float dx = rawDx * GH_MOUSE_SENSITIVITY;
            float dy = rawDy * GH_MOUSE_SENSITIVITY;

            Vector3 rot = main_camera.transform.eular;
            main_camera.setEular(rot.x + dy, rot.y + dx, 0.0f);
        }

        Matrix4 camMat = main_camera.localToWorld();

        Vector3 forward =
                camMat.transformDirection(new Vector3(0, 0, -1)).unit_vector();

        Vector3 right =
                camMat.transformDirection(new Vector3(1, 0, 0)).unit_vector();

        float wx = key_input[3] ? GH_WALK_SPEED :
                key_input[1] ? -GH_WALK_SPEED : 0.0f;

        float wz = key_input[0] ? GH_WALK_SPEED :
                key_input[2] ? -GH_WALK_SPEED : 0.0f;

        Vector3 mv = forward.mult(wz).add(right.mult(wx));
        Vector3 pos = main_camera.transform.position.add(mv);

        main_camera.setPosition(pos);
        main_camera.update();
    }
}