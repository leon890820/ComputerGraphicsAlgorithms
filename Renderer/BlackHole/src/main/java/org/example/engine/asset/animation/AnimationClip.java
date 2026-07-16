package org.example.engine.asset.animation;

import java.util.ArrayList;

public class AnimationClip {
    public String name = "";
    public double duration = 0.0;
    public double ticksPerSecond = 0.0;
    public final ArrayList<NodeAnimation> channels = new ArrayList<>();
}
