package org.example.engine.model.skin;

import org.example.engine.math.Matrix4;

public class ModelBone {
    public String name = "";
    public int nodeIndex = -1;
    public Matrix4 inverseBindMatrix = Matrix4.Identity();
}
