package org.example.engine.gameobject;

import org.example.engine.material.Material;
import org.example.engine.math.Matrix4;
import org.example.engine.mesh.SubMesh;
import org.example.engine.model.ModelAsset;
import org.example.engine.model.adapter.ModelAssetMeshAdapter;
import org.example.engine.model.importer.AssimpModelLoader;
import org.example.engine.model.runtime.ModelAnimator;

public class AssimpObject extends GameObject {

    public ModelAsset modelAsset;
    public ModelAnimator animator;

    public AssimpObject() {
    }

    public AssimpObject(String path, Material mat) {
        modelAsset = new AssimpModelLoader().load(path);
        animator = new ModelAnimator(modelAsset);
        animator.play("smolguraAnimationsRESOURCE");
        setMesh(new ModelAssetMeshAdapter().toMesh(modelAsset));
        buildSubMeshRenderers(mat);
    }

    public void updateAnimation(float time) {
        if (animator != null) {
            animator.updateAbsolute(time);
        }
    }

    public ModelAnimator getAnimator() {
        return animator;
    }

    @Override
    public Matrix4[] getBoneMatricesForSubMesh(SubMesh subMesh) {
        if (animator == null || subMesh == null || !subMesh.hasSkinWeights()) {
            return null;
        }

        return animator.getBoneMatrices(subMesh.skinIndex);
    }
}
