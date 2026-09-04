package org.example.experiments;

import org.example.engine.gl.Texture3D;
import org.example.engine.gl.Texture3DData;

import java.io.IOException;
import java.nio.file.Path;

public final class Texture3DDataExample {
    private Texture3DDataExample() {
    }

    public static void saveExample(Path path) throws IOException {
        Texture3DData data = new Texture3DData(16, 16, 16);

        for (int z = 0; z < data.getDepth(); z++) {
            for (int y = 0; y < data.getHeight(); y++) {
                for (int x = 0; x < data.getWidth(); x++) {
                    float u = x / (float) (data.getWidth() - 1);
                    float v = y / (float) (data.getHeight() - 1);
                    float w = z / (float) (data.getDepth() - 1);
                    data.setVoxel(x, y, z, u, v, w, 1.0f);
                }
            }
        }

        data.save(path);
    }

    public static Texture3D loadAndUpload(Path path) throws IOException {
        Texture3DData data = Texture3DData.load(path);
        return data.upload();
    }
}
