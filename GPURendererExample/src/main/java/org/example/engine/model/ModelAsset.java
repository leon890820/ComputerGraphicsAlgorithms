package org.example.engine.model;

import org.example.engine.model.animation.ModelAnimationClip;
import org.example.engine.model.material.ModelMaterial;
import org.example.engine.model.mesh.ModelMesh;
import org.example.engine.model.skin.ModelSkin;

import java.util.ArrayList;
import java.util.HashMap;

public class ModelAsset {
    public String name = "";
    public String sourcePath = "";

    public final ArrayList<ModelNode> nodes = new ArrayList<>();
    public final ArrayList<Integer> rootNodeIndices = new ArrayList<>();
    public final HashMap<String, Integer> nodeNameToIndex = new HashMap<>();

    public final ArrayList<ModelMesh> meshes = new ArrayList<>();
    public final ArrayList<ModelMaterial> materials = new ArrayList<>();
    public final ArrayList<ModelSkin> skins = new ArrayList<>();
    public final ArrayList<ModelAnimationClip> animations = new ArrayList<>();

    public boolean hasAnimations() {
        return !animations.isEmpty();
    }

    public boolean hasSkins() {
        return !skins.isEmpty();
    }
}
