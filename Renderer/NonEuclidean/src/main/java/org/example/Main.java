package org.example;

import org.example.engine.core.Window;
import org.example.engine.component.player.FirstPersonPlayerController;
import org.example.engine.math.*;
import org.example.engine.player.PlayerInput;
import org.example.engine.render.*;
import org.example.engine.scene.*;
import org.example.scenes.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Main {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;

    private static final float MOUSE_DEAD_ZONE = 2;

    private final PlayerInput playerInput = new PlayerInput();

    private boolean mouseInitialized = false;

    private double mouseX;
    private double mouseY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private Window window;
    private Camera main_camera;
    private FirstPersonPlayerController playerController;
    private Scene scene;
    private Renderer renderer;
    private RenderContext ctx;
    private IScene currentScene;
    private SceneType currentSceneType = SceneType.A;

    private float a = 0;
    private double lastFrameTime;

    private enum SceneType {
        A,
        B,
        C,
        D
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        setup();

        try {
            while (!window.shouldClose()) {
                draw();
            }
        } finally {
            if (scene != null) {
                scene.clear();
            }
            if (window != null) {
                window.destroy();
            }
        }
    }

    private void setup() {
        window = new Window(WIDTH, HEIGHT, "GPU Renderer - NonEuclidean Level1");
        window.create();
        lastFrameTime = glfwGetTime();

        setupInput(window);

        main_camera = new Camera();
        main_camera.transform.setPosition(0, 1.0f, 3.0f);
        playerController = new FirstPersonPlayerController(playerInput);
        main_camera.addComponent(playerController);

        renderer = new Renderer(WIDTH, HEIGHT);
        setScene(SceneType.A);
    }

    private void draw() {
        double now = glfwGetTime();
        float deltaTime = (float) (now - lastFrameTime);
        lastFrameTime = now;

        updatePlayerInput(window);
        main_camera.updateComponents(deltaTime);
        main_camera.update();
        playerInput.endFrame();

        scene.update(deltaTime);
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
                playerInput.forward = pressed;
            }

            if (key == GLFW_KEY_A) {
                playerInput.left = pressed;
            }

            if (key == GLFW_KEY_S) {
                playerInput.backward = pressed;
            }

            if (key == GLFW_KEY_D) {
                playerInput.right = pressed;
            }

            if (key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT) {
                playerInput.sprint = pressed;
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

            if (key == GLFW_KEY_3 && action == GLFW_PRESS) {
                setScene(SceneType.C);
            }

            if (key == GLFW_KEY_4 && action == GLFW_PRESS) {
                setScene(SceneType.D);
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

    private void updatePlayerInput(Window window) {
        long handle = window.getHandle();

        playerInput.lookActive =
                glfwGetMouseButton(handle, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;

        if (playerInput.lookActive) {
            float rawDx = (float) mouseDeltaX;
            float rawDy = (float) mouseDeltaY;

            if (Math.abs(rawDx) < MOUSE_DEAD_ZONE) {
                rawDx = 0.0f;
            }

            if (Math.abs(rawDy) < MOUSE_DEAD_ZONE) {
                rawDy = 0.0f;
            }

            playerInput.mouseDeltaX = rawDx;
            playerInput.mouseDeltaY = rawDy;
        }
        mouseDeltaX = 0.0;
        mouseDeltaY = 0.0;
    }

    private void setScene(SceneType sceneType) {
        if (scene != null) {
            scene.clear();
        }

        currentSceneType = sceneType;

        if (currentSceneType == SceneType.A) {
            currentScene = new SceneA();
        } else if (currentSceneType == SceneType.B) {
            currentScene = new SceneB();
        } else if (currentSceneType == SceneType.C) {
            currentScene = new SceneC();
        } else {
            currentScene = new SceneD();
        }

        scene = currentScene.load(main_camera, WIDTH, HEIGHT);
        playerController
                .resetMotion()
                .setWalkSpeed(currentScene.getWalkSpeed())
                .setScene(scene);
        ctx = new RenderContext(scene, main_camera, WIDTH, HEIGHT);
        a = 0.0f;
    }
}
