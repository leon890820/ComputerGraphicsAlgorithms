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
    private static final String WINDOW_TITLE = "GPU Renderer - SceneD Compute Example";
    private static final float COLLIDER_RESIZE_SPEED = 1.5f;
    private static final float COLLIDER_MOVE_SPEED = 1.0f;

    private final boolean[] key_input = new boolean[4];
    private final boolean[] colliderInput = new boolean[10];

    private boolean mouseInitialized = false;

    private double mouseX;
    private double mouseY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private boolean leftMouseDown;
    private boolean leftMousePressed;
    private boolean leftMouseReleased;
    private boolean sceneDControlSlime = true;

    private Window window;
    private Camera main_camera;
    private Scene scene;
    private Renderer renderer;
    private RenderContext ctx;
    private IScene currentScene;
    private SceneType currentSceneType = SceneType.D;

    private float a = 0;
    private double fpsTimer;
    private double lastFrameTime;
    private int frameCount;

    private static final int COLLIDER_GROW = 0;
    private static final int COLLIDER_SHRINK = 1;
    private static final int COLLIDER_X_POS = 2;
    private static final int COLLIDER_X_NEG = 3;
    private static final int COLLIDER_Y_POS = 4;
    private static final int COLLIDER_Y_NEG = 5;
    private static final int COLLIDER_Z_POS = 6;
    private static final int COLLIDER_Z_NEG = 7;
    private static final int COLLIDER_CENTER_UP = 8;
    private static final int COLLIDER_CENTER_DOWN = 9;

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
        window = new Window(WIDTH, HEIGHT, WINDOW_TITLE);
        window.create();
        fpsTimer = glfwGetTime();
        lastFrameTime = fpsTimer;

        setupInput(window);

        main_camera = new Camera();
        main_camera.transform.setPosition(0, 1.0f, 3.0f);

        renderer = new Renderer(WIDTH, HEIGHT);
        setScene(SceneType.D);
    }

    private void draw() {
        double now = glfwGetTime();
        float deltaTime = (float) (now - lastFrameTime);
        lastFrameTime = now;

        move(window);
        updateSceneDControls(deltaTime);
        updateSceneDGizmo();
        updateSceneDCameraFollow();

        scene.update(deltaTime);
        currentScene.update(a);
        a += 0.02f;

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderer.render(ctx);

        window.swapBuffers();
        window.pollEvents();
        updateWindowTitleFps();
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

            if (key == GLFW_KEY_3 && action == GLFW_PRESS) {
                setScene(SceneType.C);
            }

            if (key == GLFW_KEY_4 && action == GLFW_PRESS) {
                setScene(SceneType.D);
            }

            if (key == GLFW_KEY_Q && action == GLFW_PRESS) {
                toggleSceneDGizmoMode();
            }

            if (key == GLFW_KEY_R && action == GLFW_PRESS) {
                toggleSceneDFluidRenderMode();
            }

            if (key == GLFW_KEY_G && action == GLFW_PRESS) {
                toggleSceneDDebugDraw();
            }

            if (key == GLFW_KEY_TAB && action == GLFW_PRESS) {
                toggleSceneDMoveTarget();
            }

            updateColliderInput(key, pressed);
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

        glfwSetMouseButtonCallback(handle, (w, button, action, mods) -> {
            if (button != GLFW_MOUSE_BUTTON_LEFT) {
                return;
            }

            leftMouseDown = action != GLFW_RELEASE;
            leftMousePressed = action == GLFW_PRESS;
            leftMouseReleased = action == GLFW_RELEASE;
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

        if (!(currentScene instanceof SceneD) || !sceneDControlSlime) {
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
        }
        main_camera.update();
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
        ctx = new RenderContext(scene, main_camera, WIDTH, HEIGHT);
        sceneDControlSlime = true;
        a = 0.0f;
    }

    private void updateColliderInput(int key, boolean pressed) {
        if (key == GLFW_KEY_EQUAL || key == GLFW_KEY_KP_ADD) {
            colliderInput[COLLIDER_GROW] = pressed;
        }

        if (key == GLFW_KEY_MINUS || key == GLFW_KEY_KP_SUBTRACT) {
            colliderInput[COLLIDER_SHRINK] = pressed;
        }

        if (key == GLFW_KEY_RIGHT) {
            colliderInput[COLLIDER_X_POS] = pressed;
        }

        if (key == GLFW_KEY_LEFT) {
            colliderInput[COLLIDER_X_NEG] = pressed;
        }

        if (key == GLFW_KEY_UP) {
            colliderInput[COLLIDER_Y_POS] = pressed;
        }

        if (key == GLFW_KEY_DOWN) {
            colliderInput[COLLIDER_Y_NEG] = pressed;
        }

        if (key == GLFW_KEY_PAGE_UP) {
            colliderInput[COLLIDER_Z_POS] = pressed;
        }

        if (key == GLFW_KEY_PAGE_DOWN) {
            colliderInput[COLLIDER_Z_NEG] = pressed;
        }

        if (key == GLFW_KEY_HOME) {
            colliderInput[COLLIDER_CENTER_UP] = pressed;
        }

        if (key == GLFW_KEY_END) {
            colliderInput[COLLIDER_CENTER_DOWN] = pressed;
        }
    }

    private void updateSceneDControls(float deltaTime) {
        if (!(currentScene instanceof SceneD)) {
            return;
        }

        SceneD sceneD = (SceneD) currentScene;
        float resizeStep = COLLIDER_RESIZE_SPEED * deltaTime;
        float moveStep = COLLIDER_MOVE_SPEED * deltaTime;

        float uniformDelta =
                (colliderInput[COLLIDER_GROW] ? resizeStep : 0.0f) +
                (colliderInput[COLLIDER_SHRINK] ? -resizeStep : 0.0f);

        float xDelta =
                (colliderInput[COLLIDER_X_POS] ? moveStep : 0.0f) +
                (colliderInput[COLLIDER_X_NEG] ? -moveStep : 0.0f);

        float yDelta =
                (colliderInput[COLLIDER_Y_POS] ? moveStep : 0.0f) +
                (colliderInput[COLLIDER_Y_NEG] ? -moveStep : 0.0f);

        float zDelta =
                (colliderInput[COLLIDER_Z_POS] ? moveStep : 0.0f) +
                (colliderInput[COLLIDER_Z_NEG] ? -moveStep : 0.0f);

        float centerYDelta =
                (colliderInput[COLLIDER_CENTER_UP] ? moveStep : 0.0f) +
                (colliderInput[COLLIDER_CENTER_DOWN] ? -moveStep : 0.0f);

        if (uniformDelta != 0.0f) {
            sceneD.addColliderUniformSize(uniformDelta);
        }

        if (sceneDControlSlime) {
            Vector3 wasdDelta = calculateSceneDWasdDelta(moveStep);
            if (!wasdDelta.near_zero()) {
                sceneD.addSlimePosition(wasdDelta);
            }
        }

        if (xDelta != 0.0f || yDelta != 0.0f || zDelta != 0.0f) {
            sceneD.addSlimePosition(xDelta, yDelta, zDelta);
        }

        if (centerYDelta != 0.0f) {
            sceneD.addSlimePosition(0.0f, centerYDelta, 0.0f);
        }
    }

    private void updateSceneDGizmo() {
        if (currentScene instanceof SceneD) {
            SceneD sceneD = (SceneD) currentScene;
            sceneD.updateColliderGizmo(
                    main_camera,
                    WIDTH,
                    HEIGHT,
                    mouseX,
                    mouseY,
                    leftMouseDown,
                    leftMousePressed,
                    leftMouseReleased
            );
        }

        leftMousePressed = false;
        leftMouseReleased = false;
    }

    private Vector3 calculateSceneDWasdDelta(float moveStep) {
        float wx = (key_input[3] ? moveStep : 0.0f) +
                (key_input[1] ? -moveStep : 0.0f);

        float wz = (key_input[0] ? moveStep : 0.0f) +
                (key_input[2] ? -moveStep : 0.0f);

        if (wx == 0.0f && wz == 0.0f) {
            return Vector3.Zero();
        }

        Matrix4 camMat = main_camera.localToWorld();
        Vector3 forward = camMat.transformDirection(new Vector3(0, 0, -1));
        forward.y = 0.0f;
        forward = forward.unit_vector();

        Vector3 right = camMat.transformDirection(new Vector3(1, 0, 0));
        right.y = 0.0f;
        right = right.unit_vector();

        Vector3 direction = forward.mult(wz).add(right.mult(wx));
        if (direction.near_zero()) {
            return Vector3.Zero();
        }

        return direction.unit_vector().mult(moveStep);
    }

    private void updateSceneDCameraFollow() {
        if (currentScene instanceof SceneD && sceneDControlSlime) {
            SceneD sceneD = (SceneD) currentScene;
            sceneD.updateCameraFollow(main_camera);
        }
    }

    private void toggleSceneDMoveTarget() {
        if (!(currentScene instanceof SceneD)) {
            return;
        }

        sceneDControlSlime = !sceneDControlSlime;
        SceneD sceneD = (SceneD) currentScene;
        sceneD.resetCameraFollowAnchor();

        System.out.println("[SceneD] WASD target = " + getSceneDMoveTargetName());
    }

    private void toggleSceneDGizmoMode() {
        if (currentScene instanceof SceneD) {
            SceneD sceneD = (SceneD) currentScene;
            sceneD.toggleColliderGizmoMode();
        }
    }

    private void toggleSceneDFluidRenderMode() {
        if (currentScene instanceof SceneD) {
            SceneD sceneD = (SceneD) currentScene;
            sceneD.toggleFluidRenderMode();
        }
    }

    private void toggleSceneDDebugDraw() {
        if (currentScene instanceof SceneD) {
            SceneD sceneD = (SceneD) currentScene;
            sceneD.toggleDebugDraw();
        }
    }

    private void updateWindowTitleFps() {
        frameCount++;

        double now = glfwGetTime();
        double elapsed = now - fpsTimer;

        if (elapsed < 1.0) {
            return;
        }

        int fps = (int) Math.round(frameCount / elapsed);
        window.setTitle(WINDOW_TITLE + " | FPS: " + fps + " | Move: " + getSceneDMoveTargetName());

        frameCount = 0;
        fpsTimer = now;
    }

    private String getSceneDMoveTargetName() {
        if (!(currentScene instanceof SceneD)) {
            return "Camera";
        }

        return sceneDControlSlime ? "Slime" : "Camera";
    }
}
