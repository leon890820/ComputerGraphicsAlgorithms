package org.example.engine.raytracing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RayTracingBvhCache {
    private static final Map<String, CachedBvh> CACHE = new HashMap<>();
    private final RayTracingBvhBuilder bvhBuilder = new RayTracingBvhBuilder();

    public CachedBvh getOrBuild(String key, ArrayList<RayTracingTriangle> triangles, int depth, RayTracingBounds bounds) {
        CachedBvh cached = CACHE.get(key);
        if (cached != null) {
            System.out.println("[RayTracingBvhCache] hit " + key);
            return cached;
        }

        long startTime = System.nanoTime();
        RayTracingBvhBuilder.BvhBuildResult bvh = bvhBuilder.build(triangles, depth);
        CachedBvh built = new CachedBvh(bvh, bounds);
        CACHE.put(key, built);

        float elapsedMs = (System.nanoTime() - startTime) / 1_000_000.0f;
        System.out.println("[RayTracingBvhCache] built " + triangles.size() + " triangles in " + elapsedMs + " ms");
        return built;
    }

    public static class CachedBvh {
        public final RayTracingBvhBuilder.BvhBuildResult bvh;
        public final RayTracingBounds bounds;

        public CachedBvh(RayTracingBvhBuilder.BvhBuildResult bvh, RayTracingBounds bounds) {
            this.bvh = bvh;
            this.bounds = bounds;
        }
    }
}
