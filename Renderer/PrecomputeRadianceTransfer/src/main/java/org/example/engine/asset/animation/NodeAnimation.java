package org.example.engine.asset.animation;

import org.example.engine.math.Vector3;
import org.example.engine.math.Vector4;

import java.util.ArrayList;

public class NodeAnimation {
    public int nodeIndex = -1;
    public String nodeName = "";

    public final ArrayList<VectorKeyframe> positionKeys = new ArrayList<>();
    public final ArrayList<QuaternionKeyframe> rotationKeys = new ArrayList<>();
    public final ArrayList<VectorKeyframe> scaleKeys = new ArrayList<>();

    public static class VectorKeyframe {
        public double time;
        public Vector3 value;

        public VectorKeyframe(double time, Vector3 value) {
            this.time = time;
            this.value = value;
        }
    }

    public static class QuaternionKeyframe {
        public double time;
        public Vector4 value;

        public QuaternionKeyframe(double time, Vector4 value) {
            this.time = time;
            this.value = value;
        }
    }
}
