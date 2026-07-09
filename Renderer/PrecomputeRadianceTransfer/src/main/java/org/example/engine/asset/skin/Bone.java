package org.example.engine.asset.skin;

import org.example.engine.math.Matrix4;

public class Bone {
    public String name = "";
    public int nodeIndex = -1;
    public Matrix4 inverseBindMatrix = Matrix4.Identity();
}
