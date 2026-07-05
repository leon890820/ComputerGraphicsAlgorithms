package org.example.engine.mesh;

import org.example.engine.gl.Texture;
import org.example.engine.math.Vector3;

public class MtlMaterial {

    public String name;

    // ===== Texture path（OBJ/MTL 原始資料）=====
    public String mapKd;   // diffuse
    public String mapKa;   // ambient
    public String mapKs;   // specular
    public String bump;
    public String mapBump;

    // ===== GPU Texture（真正用來 render）=====
    public Texture texKd;
    public Texture texKa;
    public Texture texKs;
    public Texture texNormal;

    // ===== 顏色參數 =====
    public Vector3 Ka = new Vector3(0, 0, 0);
    public Vector3 Kd = new Vector3(1, 1, 1);
    public Vector3 Ks = new Vector3(0, 0, 0);

    public float Ns = 0.0f;
    public float d = 1.0f;
    public float Tr = 0.0f;
    public int illum = 0;

    // ===== helper =====

    public boolean hasDiffuseMap() {
        return texKd != null && texKd.isUploaded();
    }

    public boolean hasAmbientMap() {
        return texKa != null && texKa.isUploaded();
    }

    public boolean hasSpecularMap() {
        return texKs != null && texKs.isUploaded();
    }

    public boolean hasNormalMap() {
        return texNormal != null && texNormal.isUploaded();
    }

    @Override
    public String toString() {
        return "MtlMaterial{name=" + name + ", mapKd=" + mapKd + "}";
    }
}