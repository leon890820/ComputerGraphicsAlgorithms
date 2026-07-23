package org.example.engine.material;

import org.example.engine.math.Vector3;

public class FluidMeshMaterial extends Material {
    private Vector3 fluidColor = new Vector3(0.0f, 1.0f, 0.74f);
    private Vector3 rimColor = new Vector3(0.45f, 1.0f, 0.82f);
    private float rimPower = 2.4f;
    private float rimStrength = 0.85f;
    private float specularStrength = 0.55f;
    private float alpha = 0.86f;

    public FluidMeshMaterial() {
        super("/shaders/particle/shader/fluid_mesh.frag", "/shaders/core/BlinnPhong.vert");
    }

    public FluidMeshMaterial setSlimeSurface(
            Vector3 fluidColor,
            Vector3 rimColor,
            float rimPower,
            float rimStrength,
            float specularStrength,
            float alpha
    ) {
        if (fluidColor != null) {
            this.fluidColor = fluidColor;
        }
        if (rimColor != null) {
            this.rimColor = rimColor;
        }

        this.rimPower = Math.max(0.001f, rimPower);
        this.rimStrength = Math.max(0.0f, rimStrength);
        this.specularStrength = Math.max(0.0f, specularStrength);
        this.alpha = Math.max(0.0f, Math.min(alpha, 1.0f));
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.mvpMatrix == null) {
            return;
        }

        setMatrix4ToUniform("MVP", data.mvpMatrix);
        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        setIntToUniform("useSkinning", 0);
        setVector3ToUniform("cameraPosition", data.viewPosition);
        setVector3ToUniform("fluidColor", fluidColor);
        setVector3ToUniform("rimColor", rimColor);
        setFloatToUniform("rimPower", rimPower);
        setFloatToUniform("rimStrength", rimStrength);
        setFloatToUniform("specularStrength", specularStrength);
        setFloatToUniform("alpha", alpha);
    }
}
