package org.example.scenes;

import org.example.engine.component.ColliderGizmo;
import org.example.engine.component.DensitySurfaceProbe;
import org.example.engine.component.FluidRenderMode;
import org.example.engine.component.MeshRenderer;
import org.example.engine.gameobject.MeshObject;
import org.example.engine.gameobject.Slime;
import org.example.engine.gl.Texture;
import org.example.engine.material.FaceMaterial;
import org.example.engine.material.PhongMaterial;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;
import org.example.engine.scene.Scene;

public class SceneD implements IScene {
    private static final float EYE_HEIGHT = 0.62f;
    private static final float EYE_PITCH = 0.6f;
    private static final float EYE_SCALE = 0.2f;
    private static final float EYE_FALLBACK_SURFACE_DISTANCE = 0.25f;
    private static final float EYE_MIN_SURFACE_DISTANCE = 0.16f;
    private static final float EYE_SURFACE_OFFSET = 0.012f;
    private static final float EYE_SURFACE_ISO_LEVEL = 10.0f;
    private static final float EYE_DIRECTION_SMOOTH = 0.14f;
    private static final float EYE_POSITION_SMOOTH = 0.22f;
    private static final float EYE_ROTATION_SMOOTH = 0.18f;
    private static final int EYE_DENSITY_PROBE_STEPS = 96;
    private static final int EYE_DENSITY_PROBE_INTERVAL = 1;

    private Slime slime;
    private MeshObject eye;
    private DensitySurfaceProbe eyeSurfaceProbe;
    private Vector3 lastSlimePosition;
    private Vector3 targetEyeDirection = new Vector3(0.0f, 0.0f, 1.0f);
    private Vector3 smoothedEyeDirection = new Vector3(0.0f, 0.0f, 1.0f);
    private Vector3 smoothedEyePosition;
    private float smoothedEyeYaw = 3.1415926f;
    private float cachedEyeSurfaceDistance = EYE_FALLBACK_SURFACE_DISTANCE;
    private int eyeDensityProbeFrame;
    private boolean debugDrawEnabled = true;

    @Override
    public Scene load(Camera camera, int screenWidth, int screenHeight) {
        Camera.GH_FOV = 60.0f;
        camera.setSize(screenWidth, screenHeight, 0.1f, 1000.0f);
        camera.setPositionOrientation(new Vector3(0.0f, 1.5f, 1.5f), 0.0f, 0.0f);

        Scene scene = new Scene();
        scene.setCamera(camera);
        slime = new Slime();
        lastSlimePosition = slime.transform.position.copy();

        PhongMaterial floorMaterial =
                new PhongMaterial("/shaders/core/grid_floor.frag", "/shaders/core/BlinnPhong.vert");

        MeshObject floor = new MeshObject("/meshes/quad", floorMaterial);
        floor.setEular(3.1415926f / 2, 0.0f, 0.0f)
                .setScale(6.0f, 6.0f, 6.0f)
                .setPosition(0.0f, 0.0f, 0.0f);

        scene.addObject(floor);
        scene.addObject(slime);

        eye = createEyeObject();
        smoothedEyePosition = slime.transform.position.add(new Vector3(0.0f, EYE_HEIGHT, EYE_FALLBACK_SURFACE_DISTANCE));
        updateEyeTransform();
        scene.addObject(eye);
        return scene;
    }

    @Override
    public void update(float time) {
        updateEyeTransform(true);
    }

    @Override
    public float getWalkSpeed() {
        return 0.03f;
    }

    public void addColliderSize(float widthDelta, float heightDelta, float depthDelta) {
        if (slime == null) {
            return;
        }

        slime.getSimulator().addColliderSize(widthDelta, heightDelta, depthDelta);
    }

    public void addColliderUniformSize(float delta) {
        if (slime == null) {
            return;
        }

        slime.getSimulator().addColliderUniformSize(delta);
    }

    public void addSlimePosition(float xDelta, float yDelta, float zDelta) {
        if (slime == null) {
            return;
        }

        updateSlimeFacingDirection(new Vector3(xDelta, yDelta, zDelta));
        slime.setPosition(slime.transform.position.add(new Vector3(xDelta, yDelta, zDelta)));
        updateEyeTransform(false);
    }

    public void addSlimePosition(Vector3 delta) {
        if (delta != null) {
            addSlimePosition(delta.x, delta.y, delta.z);
        }
    }

    public void updateCameraFollow(Camera camera) {
        if (slime == null || camera == null) {
            return;
        }

        Vector3 current = slime.transform.position;
        if (lastSlimePosition == null) {
            lastSlimePosition = current.copy();
            return;
        }

        Vector3 delta = current.sub(lastSlimePosition);
        if (!delta.near_zero()) {
            camera.setPosition(camera.transform.position.add(delta));
            camera.update();
        }

        lastSlimePosition = current.copy();
    }

    public void resetCameraFollowAnchor() {
        if (slime != null) {
            lastSlimePosition = slime.transform.position.copy();
        }
    }

    public void updateColliderGizmo(
            Camera camera,
            int screenWidth,
            int screenHeight,
            double mouseX,
            double mouseY,
            boolean leftMouseDown,
            boolean leftMousePressed,
            boolean leftMouseReleased
    ) {
        if (slime == null || !debugDrawEnabled) {
            return;
        }

        slime.getSimulator().getCollider().updateGizmo(
                slime.transform,
                camera,
                screenWidth,
                screenHeight,
                mouseX,
                mouseY,
                leftMouseDown,
                leftMousePressed,
                leftMouseReleased
        );
    }

    public void toggleColliderGizmoMode() {
        if (slime == null) {
            return;
        }

        ColliderGizmo.Mode mode = slime.getSimulator().toggleColliderGizmoMode();
        System.out.println("[SceneD] Collider gizmo mode = " + mode);
    }

    public void toggleFluidRenderMode() {
        if (slime == null) {
            return;
        }

        FluidRenderMode mode = slime.toggleFluidRenderMode();
        System.out.println("[SceneD] Fluid render mode = " + mode);
    }

    public void toggleDebugDraw() {
        if (slime == null) {
            return;
        }

        debugDrawEnabled = slime.toggleDebugDrawEnabled();
        System.out.println("[SceneD] Debug draw = " + debugDrawEnabled);
    }

    public void setFluidRenderMode(FluidRenderMode mode) {
        if (slime == null) {
            return;
        }

        slime.setFluidRenderMode(mode);
    }

    private MeshObject createEyeObject() {
        Texture eyesTexture = new Texture("/textures/eyes.png");
        FaceMaterial eyeMaterial = new FaceMaterial(eyesTexture);
        MeshObject eyeObject = new MeshObject("/meshes/Face/face", eyeMaterial);
        eyeObject.setName("eye");
        eyeSurfaceProbe = eyeObject.addComponent(new DensitySurfaceProbe());

        for (MeshRenderer renderer : eyeObject.getMeshRenderers()) {
            renderer.setRenderedByDefaultPipeline(false);
        }

        return eyeObject;
    }

    private void updateEyeTransform() {
        updateEyeTransform(false);
    }

    private void updateEyeTransform(boolean allowDensityProbe) {
        if (slime == null || eye == null) {
            return;
        }

        updateSmoothedEyeDirection();

        if (allowDensityProbe) {
            eyeDensityProbeFrame++;
            if (eyeDensityProbeFrame >= EYE_DENSITY_PROBE_INTERVAL) {
                eyeDensityProbeFrame = 0;
                cachedEyeSurfaceDistance = probeEyeSurfaceDistance();
            }
        }

        Vector3 slimePosition = slime.transform.position;
        Vector3 faceOffset = smoothedEyeDirection.mult(cachedEyeSurfaceDistance + EYE_SURFACE_OFFSET);
        Vector3 targetPosition = new Vector3(
                slimePosition.x + faceOffset.x,
                slimePosition.y + EYE_HEIGHT,
                slimePosition.z + faceOffset.z
        );
        float targetYaw = (float) Math.atan2(smoothedEyeDirection.x, smoothedEyeDirection.z) + 3.1415926f;

        if (smoothedEyePosition == null) {
            smoothedEyePosition = targetPosition;
        } else {
            smoothedEyePosition = lerp(smoothedEyePosition, targetPosition, EYE_POSITION_SMOOTH);
        }
        smoothedEyeYaw = lerpAngle(smoothedEyeYaw, targetYaw, EYE_ROTATION_SMOOTH);

        eye.setPosition(
                        smoothedEyePosition.x,
                        smoothedEyePosition.y,
                        smoothedEyePosition.z
                )
                .setEular(EYE_PITCH, smoothedEyeYaw, 0.0f)
                .setScale(EYE_SCALE, EYE_SCALE, EYE_SCALE);
    }

    private void updateSlimeFacingDirection(Vector3 delta) {
        if (delta == null) {
            return;
        }

        Vector3 horizontal = new Vector3(delta.x, 0.0f, delta.z);
        if (!horizontal.near_zero()) {
            targetEyeDirection = horizontal.unit_vector();
        }
    }

    private void updateSmoothedEyeDirection() {
        Vector3 blended = lerp(smoothedEyeDirection, targetEyeDirection, EYE_DIRECTION_SMOOTH);
        if (!blended.near_zero()) {
            smoothedEyeDirection = blended.unit_vector();
        }
    }

    private float probeEyeSurfaceDistance() {
        if (slime == null || eyeSurfaceProbe == null) {
            return cachedEyeSurfaceDistance;
        }

        Vector3 boundsCenter = slime.getSimulator().getDensityBoundsCenter();
        Vector3 boundsSize = slime.getSimulator().getDensityBoundsSize();
        Vector3 origin = slime.transform.position.add(new Vector3(0.0f, EYE_HEIGHT, 0.0f));
        float maxDistance = estimateRayDistanceToDensityBounds(origin, smoothedEyeDirection, boundsCenter, boundsSize);
        if (maxDistance <= 0.0f) {
            return cachedEyeSurfaceDistance;
        }

        float distance = eyeSurfaceProbe.probeDistance(
                slime.getSimulator().getDensityVolumeTexture(),
                boundsCenter,
                boundsSize,
                origin,
                smoothedEyeDirection,
                EYE_SURFACE_ISO_LEVEL,
                maxDistance,
                EYE_DENSITY_PROBE_STEPS,
                cachedEyeSurfaceDistance
        );
        if (distance < EYE_MIN_SURFACE_DISTANCE) {
            return cachedEyeSurfaceDistance;
        }

        return lerp(cachedEyeSurfaceDistance, distance, 0.35f);
    }

    private float estimateRayDistanceToDensityBounds(
            Vector3 origin,
            Vector3 direction,
            Vector3 boundsCenter,
            Vector3 boundsSize
    ) {
        float minX = boundsCenter.x - boundsSize.x * 0.5f;
        float maxX = boundsCenter.x + boundsSize.x * 0.5f;
        float minZ = boundsCenter.z - boundsSize.z * 0.5f;
        float maxZ = boundsCenter.z + boundsSize.z * 0.5f;
        float maxDistance = 0.0f;

        if (Math.abs(direction.x) > 0.0001f) {
            float targetX = direction.x > 0.0f ? maxX : minX;
            maxDistance = Math.max(maxDistance, (targetX - origin.x) / direction.x);
        }

        if (Math.abs(direction.z) > 0.0001f) {
            float targetZ = direction.z > 0.0f ? maxZ : minZ;
            maxDistance = Math.max(maxDistance, (targetZ - origin.z) / direction.z);
        }

        return Math.max(EYE_FALLBACK_SURFACE_DISTANCE, maxDistance);
    }

    private float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }

    private Vector3 lerp(Vector3 a, Vector3 b, float t) {
        if (a == null) {
            return b == null ? Vector3.Zero() : b.copy();
        }
        if (b == null) {
            return a.copy();
        }

        float clampedT = clamp01(t);
        return new Vector3(
                lerp(a.x, b.x, clampedT),
                lerp(a.y, b.y, clampedT),
                lerp(a.z, b.z, clampedT)
        );
    }

    private float lerpAngle(float a, float b, float t) {
        float delta = wrapAngle(b - a);
        return a + delta * clamp01(t);
    }

    private float wrapAngle(float angle) {
        while (angle > Math.PI) {
            angle -= Math.PI * 2.0f;
        }

        while (angle < -Math.PI) {
            angle += Math.PI * 2.0f;
        }

        return angle;
    }
}
