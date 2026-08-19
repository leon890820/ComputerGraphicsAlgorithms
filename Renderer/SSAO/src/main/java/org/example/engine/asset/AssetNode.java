package org.example.engine.asset;

import org.example.engine.math.Matrix4;

import java.util.ArrayList;

public class AssetNode {
    public String name = "";
    public int parentIndex = -1;
    public final ArrayList<Integer> childIndices = new ArrayList<>();

    public Matrix4 localTransform = Matrix4.Identity();
    public final ArrayList<Integer> meshIndices = new ArrayList<>();
    public int meshIndex = -1;
    public int skinIndex = -1;
}
