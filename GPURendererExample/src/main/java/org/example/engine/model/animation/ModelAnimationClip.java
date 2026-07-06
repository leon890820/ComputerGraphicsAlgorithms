package org.example.engine.model.animation;

import java.util.ArrayList;

public class ModelAnimationClip {
    public String name = "";
    public double duration = 0.0;
    public double ticksPerSecond = 0.0;
    public final ArrayList<ModelNodeAnimation> channels = new ArrayList<>();
}
