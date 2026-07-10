package org.example.engine.prt;

import org.example.engine.mesh.Mesh;
import org.example.engine.mesh.SubMesh;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class TransferCache {

    private static final int MAGIC = 0x50525454; // PRTT
    private static final int VERSION = 1;

    public Path cachePath(String meshResourcePath, int bands, int sampleCount) {
        Path meshDir = resolveMeshDirectory(meshResourcePath);
        return meshDir.resolve("prt_transfer_bands" + bands + "_samples" + sampleCount + ".bin");
    }

    public Path legacyTextCachePath(String meshResourcePath, int bands, int sampleCount) {
        Path meshDir = resolveMeshDirectory(meshResourcePath);
        return meshDir.resolve("prt_transfer_bands" + bands + "_samples" + sampleCount + ".txt");
    }

    public boolean exists(String meshResourcePath, int bands, int sampleCount) {
        return Files.exists(cachePath(meshResourcePath, bands, sampleCount))
                || Files.exists(legacyTextCachePath(meshResourcePath, bands, sampleCount));
    }

    public ArrayList<TransferData> load(String meshResourcePath, Mesh mesh, int bands, int sampleCount) {
        Path path = cachePath(meshResourcePath, bands, sampleCount);
        if (!Files.exists(path)) {
            Path legacyPath = legacyTextCachePath(meshResourcePath, bands, sampleCount);
            if (Files.exists(legacyPath)) {
                ArrayList<TransferData> migrated = loadLegacyText(legacyPath, mesh, bands, sampleCount);
                save(meshResourcePath, mesh, migrated);
                System.out.println("[TransferCache] Migrated legacy text cache to binary: " + path);
                return migrated;
            }
        }

        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        ArrayList<TransferData> out = new ArrayList<>();

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            int magic = in.readInt();
            int version = in.readInt();
            int fileBands = in.readInt();
            int fileSampleCount = in.readInt();
            int subMeshCount = in.readInt();

            if (magic != MAGIC || version != VERSION) {
                throw new RuntimeException("[TransferCache] Invalid transfer cache header: " + path);
            }

            if (fileBands != bands || fileSampleCount != sampleCount) {
                throw new RuntimeException("[TransferCache] Cache setting mismatch: " + path);
            }

            if (subMeshCount != subMeshes.size()) {
                throw new RuntimeException("[TransferCache] Submesh count mismatch: file="
                        + subMeshCount + ", mesh=" + subMeshes.size());
            }

            for (SubMesh subMesh : subMeshes) {
                String materialName = in.readUTF();
                int vertexCount = in.readInt();
                int coefficientCount = in.readInt();
                TransferData data = new TransferData(vertexCount, bands, sampleCount);

                if (vertexCount != subMesh.getVertexCount()) {
                    throw new RuntimeException("[TransferCache] Vertex count mismatch for " + materialName
                            + ": file=" + vertexCount + ", mesh=" + subMesh.getVertexCount());
                }

                if (coefficientCount != data.getCoefficientCount()) {
                    throw new RuntimeException("[TransferCache] Coefficient count mismatch for " + materialName);
                }

                float[] raw = data.raw();
                for (int i = 0; i < raw.length; i++) {
                    raw[i] = in.readFloat();
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

        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException("[TransferCache] Failed to create cache directory: " + path.getParent(), e);
        }

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(first.getBands());
            out.writeInt(first.getSampleCount());
            out.writeInt(transferData.size());

            for (int i = 0; i < transferData.size(); i++) {
                SubMesh subMesh = subMeshes.get(i);
                TransferData data = transferData.get(i);

                out.writeUTF(subMesh.materialName == null ? "default" : subMesh.materialName);
                out.writeInt(data.getVertexCount());
                out.writeInt(data.getCoefficientCount());

                float[] raw = data.raw();
                for (float value : raw) {
                    out.writeFloat(value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("[TransferCache] Failed to write cache: " + path, e);
        }
    }

    private ArrayList<TransferData> loadLegacyText(Path path, Mesh mesh, int bands, int sampleCount) {
        ArrayList<SubMesh> subMeshes = mesh.getAllSubMeshes();
        ArrayList<TransferData> out = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String pendingSubMeshHeader = null;

            for (SubMesh subMesh : subMeshes) {
                int vertexCount = subMesh.getVertexCount();
                TransferData data = new TransferData(vertexCount, bands, sampleCount);

                String line = pendingSubMeshHeader;
                pendingSubMeshHeader = null;

                while (line != null || (line = reader.readLine()) != null) {
                    if (line.startsWith("submesh ")) {
                        break;
                    }
                    line = null;
                }

                if (line == null) {
                    throw new RuntimeException("[TransferCache] Missing submesh section for " + subMesh.materialName);
                }

                int loadedVertices = 0;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("submesh ")) {
                        pendingSubMeshHeader = trimmed;
                        break;
                    }

                    if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.contains("=")) {
                        continue;
                    }

                    String[] parts = trimmed.split("\\s+");
                    if (parts.length != data.getCoefficientCount() + 1) {
                        throw new RuntimeException("[TransferCache] Invalid transfer line: " + trimmed);
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
            throw new RuntimeException("[TransferCache] Failed to read legacy cache: " + path, e);
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
