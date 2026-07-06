package org.example.engine.component;

import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.math.Vector4;
import org.example.engine.asset.Asset;
import org.example.engine.asset.AssetNode;
import org.example.engine.asset.animation.AnimationClip;
import org.example.engine.asset.animation.NodeAnimation;
import org.example.engine.asset.skin.Bone;
import org.example.engine.asset.skin.Skin;

public class Animator {

    private final Asset asset;
    private AnimationClip currentClip;
    private double currentTime;
    private boolean playing = false;
    private boolean loop = true;

    private Matrix4[] localMatrices = new Matrix4[0];
    private Matrix4[] worldMatrices = new Matrix4[0];
    private Matrix4[][] skinBoneMatrices = new Matrix4[0][];

    public Animator(Asset asset) {
        this.asset = asset;
        allocatePoseBuffers();
        playFirstAnimation();
    }

    public void playFirstAnimation() {
        if (asset == null || asset.animations.isEmpty()) {
            playing = false;
            return;
        }

        play(asset.animations.get(0));
    }

    public void play(String clipName) {
        if (asset == null || clipName == null) {
            return;
        }

        for (AnimationClip clip : asset.animations) {
            if (clipName.equals(clip.name)) {
                play(clip);
                return;
            }
        }
    }

    public void play(AnimationClip clip) {
        currentClip = clip;
        currentTime = 0.0;
        playing = currentClip != null;
        updatePose(0.0);

        if (currentClip != null) {
            System.out.println("[Animator] play animation: " + currentClip.name
                    + ", duration = " + currentClip.duration
                    + ", channels = " + currentClip.channels.size());
        }
    }

    public void update(float deltaTimeSeconds) {
        if (!playing || currentClip == null) {
            return;
        }

        double ticksPerSecond = currentClip.ticksPerSecond == 0.0 ? 25.0 : currentClip.ticksPerSecond;
        currentTime += deltaTimeSeconds * ticksPerSecond;

        if (currentClip.duration > 0.0) {
            if (loop) {
                currentTime = currentTime % currentClip.duration;
            } else if (currentTime > currentClip.duration) {
                currentTime = currentClip.duration;
                playing = false;
            }
        }

        updatePose(currentTime);
    }

    public void updateAbsolute(float seconds) {
        if (!playing || currentClip == null) {
            return;
        }

        double ticksPerSecond = currentClip.ticksPerSecond == 0.0 ? 25.0 : currentClip.ticksPerSecond;
        double time = seconds * ticksPerSecond;

        if (currentClip.duration > 0.0) {
            time = loop ? time % currentClip.duration : Math.min(time, currentClip.duration);
        }

        currentTime = time;
        updatePose(currentTime);
    }

    public Matrix4[] getLocalMatrices() {
        return localMatrices;
    }

    public Matrix4[] getWorldMatrices() {
        return worldMatrices;
    }

    public Matrix4[][] getSkinBoneMatrices() {
        return skinBoneMatrices;
    }

    public Matrix4[] getBoneMatrices(int skinIndex) {
        if (skinIndex < 0 || skinIndex >= skinBoneMatrices.length) {
            return new Matrix4[0];
        }
        return skinBoneMatrices[skinIndex];
    }

    public AnimationClip getCurrentClip() {
        return currentClip;
    }

    public boolean isPlaying() {
        return playing;
    }

    public Animator setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    private void allocatePoseBuffers() {
        int count = asset == null ? 0 : asset.nodes.size();
        localMatrices = new Matrix4[count];
        worldMatrices = new Matrix4[count];

        for (int i = 0; i < count; i++) {
            localMatrices[i] = Matrix4.Identity();
            worldMatrices[i] = Matrix4.Identity();
        }

        int skinCount = asset == null ? 0 : asset.skins.size();
        skinBoneMatrices = new Matrix4[skinCount][];
        for (int skinIndex = 0; skinIndex < skinCount; skinIndex++) {
            Skin skin = asset.skins.get(skinIndex);
            skinBoneMatrices[skinIndex] = new Matrix4[skin.bones.size()];
            for (int boneIndex = 0; boneIndex < skin.bones.size(); boneIndex++) {
                skinBoneMatrices[skinIndex][boneIndex] = Matrix4.Identity();
            }
        }
    }

    private void updatePose(double animationTime) {
        if (asset == null) {
            return;
        }

        if (localMatrices.length != asset.nodes.size() || skinBoneMatrices.length != asset.skins.size()) {
            allocatePoseBuffers();
        }

        for (int i = 0; i < asset.nodes.size(); i++) {
            AssetNode node = asset.nodes.get(i);
            localMatrices[i] = node.localTransform;
        }

        if (currentClip != null) {
            for (NodeAnimation channel : currentClip.channels) {
                if (channel.nodeIndex < 0 || channel.nodeIndex >= localMatrices.length) {
                    continue;
                }

                Vector3 translation = sampleVector(channel.positionKeys, animationTime, null);
                Vector4 rotation = sampleQuaternion(channel.rotationKeys, animationTime, null);
                Vector3 scale = sampleVector(channel.scaleKeys, animationTime, null);

                Matrix4 local = localMatrices[channel.nodeIndex];
                Vector3 useTranslation = translation != null ? translation : local.translation();
                Vector4 useRotation = rotation != null ? rotation : new Vector4(0.0f, 0.0f, 0.0f, 1.0f);
                Vector3 useScale = scale != null ? scale : new Vector3(1.0f, 1.0f, 1.0f);

                localMatrices[channel.nodeIndex] = composeTRS(useTranslation, useRotation, useScale);
            }
        }

        for (Integer rootIndex : asset.rootNodeIndices) {
            updateWorldRecursive(rootIndex, Matrix4.Identity());
        }

        updateSkinBoneMatrices();
    }

    private void updateSkinBoneMatrices() {
        if (asset == null) {
            return;
        }

        for (int skinIndex = 0; skinIndex < asset.skins.size(); skinIndex++) {
            Skin skin = asset.skins.get(skinIndex);
            for (int boneIndex = 0; boneIndex < skin.bones.size(); boneIndex++) {
                Bone bone = skin.bones.get(boneIndex);
                if (bone.nodeIndex >= 0 && bone.nodeIndex < worldMatrices.length) {
                    skinBoneMatrices[skinIndex][boneIndex] = worldMatrices[bone.nodeIndex].mult(bone.inverseBindMatrix);
                } else {
                    skinBoneMatrices[skinIndex][boneIndex] = Matrix4.Identity();
                }
            }
        }
    }

    private void updateWorldRecursive(int nodeIndex, Matrix4 parentWorld) {
        if (nodeIndex < 0 || nodeIndex >= asset.nodes.size()) {
            return;
        }

        Matrix4 world = parentWorld.mult(localMatrices[nodeIndex]);
        worldMatrices[nodeIndex] = world;

        for (Integer childIndex : asset.nodes.get(nodeIndex).childIndices) {
            updateWorldRecursive(childIndex, world);
        }
    }

    private Vector3 sampleVector(java.util.ArrayList<NodeAnimation.VectorKeyframe> keys, double time, Vector3 fallback) {
        if (keys == null || keys.isEmpty()) {
            return fallback;
        }

        if (keys.size() == 1 || time <= keys.get(0).time) {
            return keys.get(0).value;
        }

        for (int i = 0; i < keys.size() - 1; i++) {
            NodeAnimation.VectorKeyframe a = keys.get(i);
            NodeAnimation.VectorKeyframe b = keys.get(i + 1);
            if (time <= b.time) {
                float t = interpolationFactor(a.time, b.time, time);
                return lerp(a.value, b.value, t);
            }
        }

        return keys.get(keys.size() - 1).value;
    }

    private Vector4 sampleQuaternion(java.util.ArrayList<NodeAnimation.QuaternionKeyframe> keys, double time, Vector4 fallback) {
        if (keys == null || keys.isEmpty()) {
            return fallback;
        }

        if (keys.size() == 1 || time <= keys.get(0).time) {
            return normalized(keys.get(0).value);
        }

        for (int i = 0; i < keys.size() - 1; i++) {
            NodeAnimation.QuaternionKeyframe a = keys.get(i);
            NodeAnimation.QuaternionKeyframe b = keys.get(i + 1);
            if (time <= b.time) {
                float t = interpolationFactor(a.time, b.time, time);
                return nlerp(a.value, b.value, t);
            }
        }

        return normalized(keys.get(keys.size() - 1).value);
    }

    private float interpolationFactor(double start, double end, double value) {
        double length = end - start;
        if (Math.abs(length) < 1e-8) {
            return 0.0f;
        }
        return (float) ((value - start) / length);
    }

    private Vector3 lerp(Vector3 a, Vector3 b, float t) {
        return new Vector3(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t
        );
    }

    private Vector4 nlerp(Vector4 a, Vector4 b, float t) {
        float dot = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
        float bx = dot < 0.0f ? -b.x : b.x;
        float by = dot < 0.0f ? -b.y : b.y;
        float bz = dot < 0.0f ? -b.z : b.z;
        float bw = dot < 0.0f ? -b.w : b.w;

        return normalized(new Vector4(
                a.x + (bx - a.x) * t,
                a.y + (by - a.y) * t,
                a.z + (bz - a.z) * t,
                a.w + (bw - a.w) * t
        ));
    }

    private Vector4 normalized(Vector4 q) {
        float len = (float) Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w);
        if (len < 1e-8f) {
            return new Vector4(0.0f, 0.0f, 0.0f, 1.0f);
        }
        return new Vector4(q.x / len, q.y / len, q.z / len, q.w / len);
    }

    private Matrix4 composeTRS(Vector3 translation, Vector4 rotation, Vector3 scale) {
        return Matrix4.Trans(translation).mult(quaternionToMatrix(rotation)).mult(Matrix4.Scale(scale));
    }

    private Matrix4 quaternionToMatrix(Vector4 q) {
        Vector4 n = normalized(q);
        float x = n.x;
        float y = n.y;
        float z = n.z;
        float w = n.w;

        Matrix4 out = Matrix4.Identity();
        out.set(0, 0, 1.0f - 2.0f * y * y - 2.0f * z * z);
        out.set(0, 1, 2.0f * x * y - 2.0f * z * w);
        out.set(0, 2, 2.0f * x * z + 2.0f * y * w);

        out.set(1, 0, 2.0f * x * y + 2.0f * z * w);
        out.set(1, 1, 1.0f - 2.0f * x * x - 2.0f * z * z);
        out.set(1, 2, 2.0f * y * z - 2.0f * x * w);

        out.set(2, 0, 2.0f * x * z - 2.0f * y * w);
        out.set(2, 1, 2.0f * y * z + 2.0f * x * w);
        out.set(2, 2, 1.0f - 2.0f * x * x - 2.0f * y * y);

        return out;
    }
}
