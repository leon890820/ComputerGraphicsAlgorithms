package org.example.engine.prt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkyboxSHCache {

    public Path cachePath(String skyboxResourcePath, int bands) {
        Path skyboxDir = resolveResourceDirectory(skyboxResourcePath);
        return skyboxDir.resolve("sh_bands" + bands + ".txt");
    }

    public boolean exists(String skyboxResourcePath, int bands) {
        return Files.exists(cachePath(skyboxResourcePath, bands));
    }

    public SHCoefficients load(String skyboxResourcePath, int bands) {
        Path path = cachePath(skyboxResourcePath, bands);

        try {
            SHCoefficients coefficients = new SHCoefficients(bands);

            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.contains("=")) {
                    continue;
                }

                String[] parts = trimmed.split("\\s+");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("[SkyboxSHCache] invalid coefficient line: " + line);
                }

                int index = Integer.parseInt(parts[0]);
                coefficients.add(
                        index,
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3])
                );
            }

            return coefficients;
        } catch (IOException e) {
            throw new RuntimeException("[SkyboxSHCache] Failed to read cache: " + path, e);
        }
    }

    public void save(String skyboxResourcePath, SHCoefficients coefficients) {
        Path path = cachePath(skyboxResourcePath, coefficients.getBands());

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(
                    path,
                    coefficients.toCacheString(skyboxResourcePath),
                    StandardCharsets.UTF_8
            );
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
