package org.example.engine.prt;

import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferCache {

    public Path cachePath(String meshResourcePath, int bands, int sampleCount) {
        Path meshDir = resolveMeshDirectory(meshResourcePath);
        return meshDir.resolve("prt_transfer_bands" + bands + "_samples" + sampleCount + ".txt");
    }

    public boolean exists(String meshResourcePath, int bands, int sampleCount) {
        return Files.exists(cachePath(meshResourcePath, bands, sampleCount));
    }

    public ArrayList<TransferData> load(String meshResourcePath, Mesh mesh, int bands, int sampleCount) {
        Path path = cachePath(meshResourcePath, bands, sampleCount);
        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        ArrayList<TransferData> out = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            int cursor = 0;

            for (SubMesh subMesh : subMeshes) {
                int vertexCount = subMesh.getVertexCount();
                TransferData data = new TransferData(vertexCount, bands, sampleCount);

                while (cursor < lines.size() && !lines.get(cursor).startsWith("submesh ")) {
                    cursor++;
                }
                if (cursor >= lines.size()) {
                    throw new RuntimeException("[TransferCache] Missing submesh section for " + subMesh.materialName);
                }
                cursor++;

                int loadedVertices = 0;
                while (cursor < lines.size()) {
                    String line = lines.get(cursor).trim();
                    if (line.startsWith("submesh ")) {
                        break;
                    }
                    cursor++;

                    if (line.isEmpty() || line.startsWith("#") || line.contains("=")) {
                        continue;
                    }

                    String[] parts = line.split("\\s+");
                    if (parts.length != data.getCoefficientCount() + 1) {
                        throw new RuntimeException("[TransferCache] Invalid transfer line: " + line);
                    }

                    int vertexIndex = Integer.parseInt(parts[0]);
                    for (int k = 0; k < data.getCoefficientCount(); k++) {
                        data.set(vertexIndex, k, Float.parseFloat(parts[k + 1]));
                    }
                    loadedVertices++;
                }

                if (loadedVertices != vertexCount) {
                    throw new RuntimeException("[TransferCache] Vertex count mismatch for " + subMesh.materialName
                            + ": file=" + loadedVertices + ", mesh=" + vertexCount);
                }

                out.add(data);
            }

            return out;
        } catch (IOException e) {
            throw new RuntimeException("[TransferCache] Failed to read cache: " + path, e);
        }
    }

    public void save(String meshResourcePath, Mesh mesh, ArrayList<TransferData> transferData) {
        if (transferData == null || transferData.isEmpty()) {
            return;
        }

        TransferData first = transferData.get(0);
        Path path = cachePath(meshResourcePath, first.getBands(), first.getSampleCount());
        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();

        StringBuilder out = new StringBuilder();
        out.append("# Per-vertex unshadowed diffuse PRT transfer coefficients\n");
        out.append("source=").append(meshResourcePath).append('\n');
        out.append("bands=").append(first.getBands()).append('\n');
        out.append("sampleCount=").append(first.getSampleCount()).append('\n');
        out.append("layout=vertexIndex coeff0 coeff1 ...\n");

        for (int i = 0; i < transferData.size(); i++) {
            SubMesh subMesh = subMeshes.get(i);
            TransferData data = transferData.get(i);
            out.append("submesh ")
                    .append(i)
                    .append(' ')
                    .append(subMesh.materialName)
                    .append(' ')
                    .append(data.getVertexCount())
                    .append('\n');

            for (int v = 0; v < data.getVertexCount(); v++) {
                out.append(v);
                for (int k = 0; k < data.getCoefficientCount(); k++) {
                    out.append(' ').append(String.format(Locale.US, "%.9g", data.get(v, k)));
                }
                out.append('\n');
            }
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, out.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("[TransferCache] Failed to write cache: " + path, e);
        }
    }

    private Path resolveMeshDirectory(String meshResourcePath) {
        String normalized = meshResourcePath.startsWith("/")
                ? meshResourcePath.substring(1)
                : meshResourcePath;

        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();

        for (Path p = cwd; p != null; p = p.getParent()) {
            Path resourceRoot = p.resolve("src/main/resources");
            Path direct = resourceRoot.resolve(normalized);
            Path parent = direct.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                return parent;
            }
        }

        throw new RuntimeException("[TransferCache] Cannot find mesh resource directory for: " + meshResourcePath);
    }
}
