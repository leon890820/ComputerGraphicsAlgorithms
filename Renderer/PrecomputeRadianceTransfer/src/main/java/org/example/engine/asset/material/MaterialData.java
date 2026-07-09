package org.example.engine.asset.material;

import org.example.engine.gl.Texture;
import org.example.engine.math.Vector3;
import org.example.engine.math.Vector4;

public class MaterialData {
    public String name = "";

    public Vector4 baseColorFactor = new Vector4(1.0f, 1.0f, 1.0f, 1.0f);
    public Vector3 emissiveFactor = new Vector3(0.0f, 0.0f, 0.0f);
    public float metallicFactor = 1.0f;
    public float roughnessFactor = 1.0f;
    public float alphaCutoff = 0.5f;

    public AlphaMode alphaMode = AlphaMode.OPAQUE;
    public boolean doubleSided = false;

    public Texture baseColorTexture;
    public Texture normalTexture;
    public Texture metallicRoughnessTexture;
    public Texture emissiveTexture;
    public Texture occlusionTexture;

    public enum AlphaMode {
        OPAQUE,
        MASK,
        BLEND
    }
}
