package org.example;

import org.example.engine.core.Window;
import org.example.engine.math.*;
import org.example.engine.render.*;
import org.example.engine.scene.*;
import org.example.scenes.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Main {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    private static final float GH_MOUSE_SENSITIVITY = 0.005f;
    private static final float MOUSE_DEAD_ZONE = 2;

    private final boolean[] key_input = new boolean[4];

    private boolean mouseInitialized = false;

    private double mouseX;
    private double mouseY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private Window window;
    private Camera main_camera;
    private Scene scene;
    private Renderer renderer;
    private RenderContext ctx;
    private IScene currentScene;
    private SceneType currentSceneType = SceneType.A;

    private float a = 0;

    private enum SceneType {
        A,
        B
    }

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
        main_camera.transform.setPosition(0, 1.0f, 3.0f);

        renderer = new Renderer(WIDTH, HEIGHT);
        setScene(SceneType.A);
    }

    private void draw() {
        move(window);

        currentScene.update(a);
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

            if (key == GLFW_KEY_1 && action == GLFW_PRESS) {
                setScene(SceneType.A);
            }

            if (key == GLFW_KEY_2 && action == GLFW_PRESS) {
                setScene(SceneType.B);
            }
        });

        glfwSetCursorPosCallback(handle, (w, x, y) -> {
            if (!mouseInitialized) {
                mouseX = x;
                mouseY = y;
                mouseInitialized = true;
                return;
            }

            mouseDeltaX += mouseX - x;
            mouseDeltaY += mouseY - y;
            mouseX = x;
            mouseY = y;
        });
    }

    private void move(Window window) {
        long handle = window.getHandle();

        boolean rightMousePressed =
                glfwGetMouseButton(handle, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;

        if (rightMousePressed) {
            float rawDx = (float) mouseDeltaX;
            float rawDy = (float) mouseDeltaY;

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
        mouseDeltaX = 0.0;
        mouseDeltaY = 0.0;

        Matrix4 camMat = main_camera.localToWorld();

        Vector3 forward =
                camMat.transformDirection(new Vector3(0, 0, -1)).unit_vector();

        Vector3 right =
                camMat.transformDirection(new Vector3(1, 0, 0)).unit_vector();

        float walkSpeed = currentScene.getWalkSpeed();

        float wx = key_input[3] ? walkSpeed :
                key_input[1] ? -walkSpeed : 0.0f;

        float wz = key_input[0] ? walkSpeed :
                key_input[2] ? -walkSpeed : 0.0f;

        Vector3 mv = forward.mult(wz).add(right.mult(wx));
        Vector3 pos = main_camera.transform.position.add(mv);

        main_camera.setPosition(pos);
        main_camera.update();
    }

    private void setScene(SceneType sceneType) {
        currentSceneType = sceneType;

        if (currentSceneType == SceneType.A) {
            currentScene = new SceneA();
        } else {
            currentScene = new SceneB();
        }

        scene = currentScene.load(main_camera, WIDTH, HEIGHT);
        ctx = new RenderContext(scene, main_camera, WIDTH, HEIGHT);
        a = 0.0f;
    }
}
