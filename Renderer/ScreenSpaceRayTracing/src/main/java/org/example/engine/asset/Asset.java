package org.example.engine.asset;

import org.example.engine.asset.animation.AnimationClip;
import org.example.engine.asset.material.MaterialData;
import org.example.engine.asset.mesh.MeshData;
import org.example.engine.asset.skin.Skin;

import java.util.ArrayList;
import java.util.HashMap;

public class Asset {
    public String name = "";
    public String sourcePath = "";

    public final ArrayList<AssetNode> nodes = new ArrayList<>();
    public final ArrayList<Integer> rootNodeIndices = new ArrayList<>();
    public final HashMap<String, Integer> nodeNameToIndex = new HashMap<>();

    public final ArrayList<MeshData> meshes = new ArrayList<>();
    public final ArrayList<MaterialData> materials = new ArrayList<>();
    public final ArrayList<Skin> skins = new ArrayList<>();
    public final ArrayList<AnimationClip> animations = new ArrayList<>();

    public boolean hasAnimations() {
        return !animations.isEmpty();
    }

    public boolean hasSkins() {
        return !skins.isEmpty();
    }
}
