package org.example;

import org.example.engine.core.Window;
import org.example.engine.gameobject.PhongObject;
import org.example.engine.gl.SSBO;
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
    private static final float GH_WALK_SPEED = 3f;
    private static final float MOUSE_DEAD_ZONE = 2;
    private static final int VPL_NUM = 32;

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

    Vector3 pointA = new Vector3(0, 300f, -800);
    Vector3 pointB = new Vector3(0, 300f,  800);

    Vector3 currentTarget = pointB;
    float speed = 200.0f;


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
        window = new Window(WIDTH, HEIGHT, "Reflective Shadow Maps");
        window.create();

        setupInput(window);
        scene = new Scene();
        main_camera = new Camera();
        main_camera.setSize(WIDTH, HEIGHT, 0.01f, 10000.0f);
        main_camera.transform.setPosition(0.0f, -300f, 800.0f).setEular(-0.1f,0.0f,0.0f);

        PhongObject sponza = new PhongObject("../../Model/sponza/Scale300Sponza",null);
        sponza.setScene(scene);
        scene.setCamera(main_camera);
        scene.addObject(sponza);

        light = new PointLight(
                new Vector3(0, -450f, -1135),
                //new Vector3(-0.5f, -1 ,-2 ),
                new Vector3(0.8f, 0.8f, 0.8f)
        );
        renderer = new Renderer(WIDTH, HEIGHT);
        ctx = new RenderContext(scene, main_camera, WIDTH, HEIGHT);

        scene.addLight(light);

        var weight = initVPLsSampleCoordsAndWeights();
        SSBO ssbo = new SSBO(0, weight);
    }

    private void draw() {
        move(window);


        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        updateLightMovement(light, 0.016f);
        renderer.render(ctx);

        window.swapBuffers();
        window.pollEvents();
    }
    //-410,0,1280
    //-410,0,-1200
    //410,0,-1200
    //410,0,1200

    void updateLightMovement(Light light, float dt) {
        Vector3 pos = light.transform.position;

        Vector3 dir = currentTarget.sub(pos);
        float dist = dir.length();

        if (dist < 1.0f) {
            // 到達後切換目標
            if (currentTarget == pointA) {
                currentTarget = pointB;
            } else {
                currentTarget = pointA;
            }
            return;
        }

        dir = dir.unit_vector();
        float step = speed * dt;

        if (step > dist) {
            light.transform.position = currentTarget.copy();
        } else {
            light.transform.position = pos.add(dir.mult(step));
        }
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

    public float[] initVPLsSampleCoordsAndWeights() {
        float[] weight = new float[VPL_NUM * 4];

        for (int i = 0; i < VPL_NUM; i++) {
            float r = (float) Math.sqrt(Math.random());
            float theta = (float) (2.0 * Math.PI * Math.random());
            Vector3 vector = Vector3.random_unit_vector();

            weight[i * 4 + 0] = vector.x;
            weight[i * 4 + 1] = vector.y;
            weight[i * 4 + 2] = vector.z;   // weight
            weight[i * 4 + 3] = 0.0f;
        }

        return weight;
    }
}