package org.example.engine.material;

import org.example.engine.gl.Texture;

import java.util.Set;

public class GBufferMaterial extends Material {

    Texture texture;
    Texture normalMap;
    public GBufferMaterial(String frag) {
        super(frag);
    }

    public GBufferMaterial(String frag, String vert) {
        super(frag, vert);
    }

    public GBufferMaterial setTexture(Texture t) {
        texture = t;
        return this;
    }

    public GBufferMaterial setNormalMap(Texture t) {
        normalMap = t;
        return this;
    }

    @Override
    @Override
    public void run(MaterialRenderData data) {
        if (data == null || data.modelMatrix == null || data.mvpMatrix == null) {
            System.out.println("[GBufferMaterial] render data is missing model or MVP matrix.");
            return;
        }

        setMatrix4ToUniform("MVP", data.mvpMatrix);
        setMatrix4ToUniform("modelMatrix", data.modelMatrix);
        setMatrix4ToUniform("u_ViewMatrix", data.viewMatrix);
        applySkinning(data);

        setVector3ToUniform("ambient_light", 0.5f, 0.5f, 0.5f);
        setVector3ToUniform("cameraPos", data.viewPosition);
        setFloatToUniform("cameraFar", data.cameraFar);


        Texture useTex = texture != null ? texture : data.baseColorTexture;

        if (useTex != null && useTex.isUploaded()) {
            setIntToUniform("hasTexture", 1);
            setTexture("tex", useTex, 0);
        } else {
            setIntToUniform("hasTexture", 0);
            setVector3ToUniform("defaultColor", 0.45f, 0.45f, 0.45f);
        }

        if (normalMap != null && normalMap.isUploaded()) {
            setIntToUniform("hasNormalMap", 1);
            setTexture("normalMap", normalMap, 1);
        } else {
            setIntToUniform("hasNormalMap", 0);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        unbindTexture(1);
    }

    @Override
    public void collectTextures(Set<Texture> textures) {
        if (texture != null) {
            textures.add(texture);
        }
        if (normalMap != null) {
            textures.add(normalMap);
        }
    }
}
