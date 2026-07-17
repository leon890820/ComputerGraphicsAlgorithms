package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.scene.Camera;

public class MaterialRenderData {

    public Matrix4 modelMatrix;
    public Matrix4 mvpMatrix;
    public Matrix4[] boneMatrices;
    public Camera camera;

    public Texture baseColorTexture;

    public Vector3 viewPosition;
    public boolean hasLight;
    public Vector3 lightPosition;
    public Vector3 lightColor;
    public Vector3 lightDirection;
    public Matrix4 lightSpaceMatrix;
    public float lightFar;
}
