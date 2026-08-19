package org.example.engine.component.player;

import org.example.engine.component.core.Component;
import org.example.engine.component.render.MeshFilter;
import org.example.engine.gameobject.GameObject;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;
import org.example.engine.player.PlayerInput;
import org.example.engine.scene.Scene;
import org.example.engine.scene.Transform;

import java.util.ArrayList;

public class FirstPersonPlayerController extends Component {
    private static final float PLAYER_HEIGHT = 1.5f;
    private static final float PLAYER_RADIUS = 0.2f;
    private static final float GRAVITY = -9.8f;
    private static final float WALK_ACCEL = 50.0f;
    private static final float DRAG = 0.002f;
    private static final float FRICTION = 0.04f;
    private static final float MOUSE_SENSITIVITY = 0.005f;
    private static final float SPRINT_MULTIPLIER = 1.8f;
    private static final float BOB_FREQ = 8.0f;
    private static final float BOB_OFFS = 0.015f;
    private static final float BOB_DAMP = 0.04f;
    private static final float BOB_MIN = 0.1f;
    private static final int COLLISION_ITERATIONS = 3;

    private final PlayerInput input;
    private final ArrayList<GameObject> colliders = new ArrayList<>();

    private Vector3 velocity = Vector3.Zero();
    private Vector3 bodyPosition = Vector3.Zero();
    private Vector3 previousBodyPosition = Vector3.Zero();
    private boolean onGround = false;
    private float bobMagnitude = 0.0f;
    private float bobPhase = 0.0f;
    private float walkSpeed = 2.9f;

    public FirstPersonPlayerController(PlayerInput input) {
        this.input = input;
    }

    public FirstPersonPlayerController setScene(Scene scene) {
        colliders.clear();
        syncBodyToTransform();
        if (scene == null) {
            return this;
        }

        for (GameObject object : scene.getObjects()) {
            if (object != null && object.getMeshFilter() != null && object.getMeshFilter().hasMesh()) {
                colliders.add(object);
            }
        }

        return this;
    }

    public FirstPersonPlayerController setWalkSpeed(float walkSpeed) {
        this.walkSpeed = Math.max(0.0f, walkSpeed);
        return this;
    }

    public FirstPersonPlayerController resetMotion() {
        syncBodyToTransform();
        velocity = Vector3.Zero();
        onGround = false;
        bobMagnitude = 0.0f;
        bobPhase = 0.0f;
        return this;
    }

    private void syncBodyToTransform() {
        if (gameObject == null) {
            return;
        }

        bodyPosition = gameObject.transform.position.copy();
        previousBodyPosition = bodyPosition.copy();
        gameObject.transform.setPosition(bodyPosition);
    }

    @Override
    public void update(float deltaTime) {
        if (gameObject == null || input == null || deltaTime <= 0.0f) {
            return;
        }

        updateLook();

        if (walkSpeed <= 0.0f) {
            bodyPosition = gameObject.transform.position.copy();
            gameObject.transform.forceDirty();
            return;
        }

        updateBobbing(deltaTime);
        previousBodyPosition = bodyPosition.copy();
        updateVelocity(deltaTime);
        moveAndCollide(deltaTime);
        applyCameraOffset();
    }

    private void updateLook() {
        if (!input.lookActive) {
            return;
        }

        Transform transform = gameObject.transform;
        float pitch = transform.eular.x + input.mouseDeltaY * MOUSE_SENSITIVITY;
        float yaw = transform.eular.y + input.mouseDeltaX * MOUSE_SENSITIVITY;
        float limit = (float) Math.toRadians(89.0f);

        pitch = Math.max(-limit, Math.min(limit, pitch));
        transform.setEular(pitch, yaw, 0.0f);
    }

    private void updateVelocity(float deltaTime) {
        velocity.y += GRAVITY * deltaTime;
        velocity = velocity.mult(1.0f - DRAG);

        float moveForward = 0.0f;
        float moveRight = 0.0f;

        if (input.forward) moveForward += 1.0f;
        if (input.backward) moveForward -= 1.0f;
        if (input.right) moveRight += 1.0f;
        if (input.left) moveRight -= 1.0f;

        float moveMag = (float) Math.sqrt(moveForward * moveForward + moveRight * moveRight);
        if (moveMag > 1.0f) {
            moveForward /= moveMag;
            moveRight /= moveMag;
        }

        float yaw = gameObject.transform.eular.y;
        Matrix4 yawMatrix = Matrix4.RotY(yaw);
        Vector3 forward = yawMatrix.transformDirection(new Vector3(0, 0, -1));
        Vector3 right = yawMatrix.transformDirection(new Vector3(1, 0, 0));

        float speedMultiplier = input.sprint ? SPRINT_MULTIPLIER : 1.0f;
        Vector3 acceleration = forward.mult(moveForward)
                .add(right.mult(moveRight))
                .mult(WALK_ACCEL * speedMultiplier * deltaTime);
        velocity = velocity.add(acceleration);

        float y = velocity.y;
        Vector3 horizontal = new Vector3(velocity.x, 0.0f, velocity.z);
        horizontal.clipMag(walkSpeed * speedMultiplier);
        velocity = new Vector3(horizontal.x, y, horizontal.z);
    }

    private void moveAndCollide(float deltaTime) {
        bodyPosition = bodyPosition.add(velocity.mult(deltaTime));
        onGround = false;

        for (int i = 0; i < COLLISION_ITERATIONS; i++) {
            boolean collided = false;
            for (GameObject collider : colliders) {
                if (collider != null) {
                    collided |= collideWithObject(collider);
                }
            }
            if (!collided) {
                break;
            }
        }
    }

    private boolean collideWithObject(GameObject object) {
        MeshFilter filter = object.getMeshFilter();
        if (filter == null || !filter.hasMesh()) {
            return false;
        }

        Mesh mesh = filter.getMesh();
        Matrix4 localToWorld = object.localToWorld();
        boolean collided = false;

        for (SubMesh subMesh : mesh.getAllSubMeshes()) {
            if (subMesh == null || subMesh.positions == null || subMesh.indices == null) {
                continue;
            }

            for (int i = 0; i + 2 < subMesh.indices.length; i += 3) {
                Vector3 a = readWorldVertex(subMesh, subMesh.indices[i], localToWorld);
                Vector3 b = readWorldVertex(subMesh, subMesh.indices[i + 1], localToWorld);
                Vector3 c = readWorldVertex(subMesh, subMesh.indices[i + 2], localToWorld);

                collided |= collideSphereWithTriangle(headSphereCenter(), PLAYER_RADIUS, a, b, c);
                collided |= collideSphereWithTriangle(feetSphereCenter(), PLAYER_RADIUS, a, b, c);
            }
        }

        return collided;
    }

    private Vector3 readWorldVertex(SubMesh subMesh, int index, Matrix4 localToWorld) {
        int base = index * 3;
        Vector3 local = new Vector3(
                subMesh.positions[base],
                subMesh.positions[base + 1],
                subMesh.positions[base + 2]
        );
        return localToWorld.transformPoint(local);
    }

    private Vector3 headSphereCenter() {
        return bodyPosition;
    }

    private Vector3 feetSphereCenter() {
        return bodyPosition.add(new Vector3(0.0f, PLAYER_RADIUS - PLAYER_HEIGHT, 0.0f));
    }

    private boolean collideSphereWithTriangle(Vector3 center, float radius, Vector3 a, Vector3 b, Vector3 c) {
        Vector3 closest = closestPointOnTriangle(center, a, b, c);
        Vector3 delta = center.sub(closest);
        float distSq = delta.magSq();
        float radiusSq = radius * radius;

        if (distSq >= radiusSq) {
            return false;
        }

        Vector3 normal;
        float dist = (float) Math.sqrt(distSq);
        if (dist > 1e-6f) {
            normal = delta.mult(1.0f / dist);
        } else {
            normal = Vector3.cross(b.sub(a), c.sub(a)).unit_vector();
            if (Vector3.dot(normal, bodyPosition.sub(a)) < 0.0f) {
                normal = normal.mult(-1.0f);
            }
        }

        float pushAmount = radius - dist + 0.0005f;
        Vector3 push = normal.mult(pushAmount);
        bodyPosition = bodyPosition.add(push);
        applyCollisionResponse(push);
        return true;
    }

    private void updateBobbing(float deltaTime) {
        float speed = bodyPosition.sub(previousBodyPosition).length() / Math.max(deltaTime, 1e-6f);
        if (!onGround) {
            speed = 0.0f;
        }

        bobMagnitude = bobMagnitude * (1.0f - BOB_DAMP) + speed * BOB_DAMP;
        if (bobMagnitude < BOB_MIN) {
            bobPhase = 0.0f;
        } else {
            bobPhase += BOB_FREQ * deltaTime;
            float twoPi = (float) Math.PI * 2.0f;
            if (bobPhase > twoPi) {
                bobPhase -= twoPi;
            }
        }
    }

    private void applyCameraOffset() {
        float yOffset = 0.0f;
        if (bobMagnitude >= BOB_MIN) {
            float theta = ((float) Math.PI / 2.0f) * (float) Math.sin(bobPhase);
            yOffset = bobMagnitude * BOB_OFFS * (1.0f - (float) Math.cos(theta));
        }

        gameObject.transform.setPosition(bodyPosition.add(new Vector3(0.0f, yOffset, 0.0f)));
        gameObject.transform.forceDirty();
    }

    private void applyCollisionResponse(Vector3 push) {
        if (push.magSq() < 1e-10f) {
            return;
        }

        Vector3 pushDir = push.unit_vector();
        if (pushDir.y > 0.7f) {
            onGround = true;
        }

        float pushDot = Vector3.dot(velocity, push);
        if (pushDot >= 0.0f) {
            return;
        }

        Vector3 pushProjection = push.mult(pushDot / Math.max(push.magSq(), 1e-8f));
        float friction = onGround ? FRICTION : 0.0f;
        velocity = velocity.sub(pushProjection).mult(1.0f - friction);
    }

    private Vector3 closestPointOnTriangle(Vector3 p, Vector3 a, Vector3 b, Vector3 c) {
        Vector3 ab = b.sub(a);
        Vector3 ac = c.sub(a);
        Vector3 ap = p.sub(a);
        float d1 = Vector3.dot(ab, ap);
        float d2 = Vector3.dot(ac, ap);
        if (d1 <= 0.0f && d2 <= 0.0f) return a;

        Vector3 bp = p.sub(b);
        float d3 = Vector3.dot(ab, bp);
        float d4 = Vector3.dot(ac, bp);
        if (d3 >= 0.0f && d4 <= d3) return b;

        float vc = d1 * d4 - d3 * d2;
        if (vc <= 0.0f && d1 >= 0.0f && d3 <= 0.0f) {
            float v = d1 / (d1 - d3);
            return a.add(ab.mult(v));
        }

        Vector3 cp = p.sub(c);
        float d5 = Vector3.dot(ab, cp);
        float d6 = Vector3.dot(ac, cp);
        if (d6 >= 0.0f && d5 <= d6) return c;

        float vb = d5 * d2 - d1 * d6;
        if (vb <= 0.0f && d2 >= 0.0f && d6 <= 0.0f) {
            float w = d2 / (d2 - d6);
            return a.add(ac.mult(w));
        }

        float va = d3 * d6 - d5 * d4;
        if (va <= 0.0f && (d4 - d3) >= 0.0f && (d5 - d6) >= 0.0f) {
            float w = (d4 - d3) / ((d4 - d3) + (d5 - d6));
            return b.add(c.sub(b).mult(w));
        }

        float denom = 1.0f / (va + vb + vc);
        float v = vb * denom;
        float w = vc * denom;
        return a.add(ab.mult(v)).add(ac.mult(w));
    }
}
