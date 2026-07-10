package org.example.engine.prt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkyboxSHCache {

    private static final int MAGIC = 0x5348534B; // SHSK
    private static final int VERSION = 1;

    public Path cachePath(String skyboxResourcePath, int bands) {
        Path skyboxDir = resolveResourceDirectory(skyboxResourcePath);
        return skyboxDir.resolve("sh_bands" + bands + ".bin");
    }

    public boolean exists(String skyboxResourcePath, int bands) {
        return Files.exists(cachePath(skyboxResourcePath, bands));
    }

    public SHCoefficients load(String skyboxResourcePath, int bands) {
        Path path = cachePath(skyboxResourcePath, bands);

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            int magic = in.readInt();
            int version = in.readInt();
            int fileBands = in.readInt();
            int coefficientCount = in.readInt();
            SHCoefficients coefficients = new SHCoefficients(bands);

            if (magic != MAGIC || version != VERSION || fileBands != bands
                    || coefficientCount != coefficients.getCoefficientCount()) {
                throw new RuntimeException("[SkyboxSHCache] Invalid SH cache header: " + path);
            }

            for (int i = 0; i < coefficientCount; i++) {
                coefficients.add(
                        i,
                        in.readFloat(),
                        in.readFloat(),
                        in.readFloat()
                );
            }

            return coefficients;
        } catch (IOException e) {
            throw new RuntimeException("[SkyboxSHCache] Failed to read cache: " + path, e);
        }
    }

    public void save(String skyboxResourcePath, SHCoefficients coefficients) {
        Path path = cachePath(skyboxResourcePath, coefficients.getBands());

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            Files.createDirectories(path.getParent());

            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(coefficients.getBands());
            out.writeInt(coefficients.getCoefficientCount());

            for (int i = 0; i < coefficients.getCoefficientCount(); i++) {
                out.writeFloat(coefficients.r(i));
                out.writeFloat(coefficients.g(i));
                out.writeFloat(coefficients.b(i));
            }
        } catch (IOException e) {
            throw new RuntimeException("[SkyboxSHCache] Failed to write cache: " + path, e);
        }
    }

    private Path resolveResourceDirectory(String resourcePath) {
        String normalized = resourcePath.startsWith("/")
                ? resourcePath.substring(1)
                : resourcePath;

        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();

        for (Path p = cwd; p != null; p = p.getParent()) {
            Path candidate = p.resolve("src/main/resources").resolve(normalized);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        throw new RuntimeException("[SkyboxSHCache] Cannot find resource directory for: " + resourcePath);
    }
}
