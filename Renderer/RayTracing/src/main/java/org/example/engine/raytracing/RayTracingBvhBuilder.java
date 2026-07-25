package org.example.engine.raytracing;

import org.example.engine.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class RayTracingBvhBuilder {
    public static final int MAX_TRIANGLES_PER_LEAF = 2;
    public static final int SAH_BIN_COUNT = 16;

    public BvhBuildResult build(List<RayTracingTriangle> triangles, int depth) {
        BvhNode root = new BvhNode(new ArrayList<>(triangles), depth);
        ArrayList<NodeData> nodes = new ArrayList<>();
        ArrayList<RayTracingTriangle> orderedTriangles = new ArrayList<>();
        flatten(root, nodes, orderedTriangles);
        if (orderedTriangles.size() != triangles.size()) {
            System.out.println("[RayTracingBvhBuilder] Warning: BVH triangle count mismatch, input="
                    + triangles.size() + ", output=" + orderedTriangles.size());
        }
        return new BvhBuildResult(nodes, orderedTriangles);
    }

    private int flatten(BvhNode node, ArrayList<NodeData> nodes, ArrayList<RayTracingTriangle> orderedTriangles) {
        int nodeIndex = nodes.size();
        NodeData nodeData = new NodeData();
        nodeData.bounds = node.bounds;
        nodes.add(nodeData);

        if (node.childA != null && node.childB != null) {
            nodeData.childAIndex = flatten(node.childA, nodes, orderedTriangles);
            nodeData.childBIndex = flatten(node.childB, nodes, orderedTriangles);
            return nodeIndex;
        }

        nodeData.triangleIndex = orderedTriangles.size();
        nodeData.triangleSize = node.triangles.size();
        orderedTriangles.addAll(node.triangles);
        return nodeIndex;
    }

    public static class BvhBuildResult {
        public final ArrayList<NodeData> nodes;
        public final ArrayList<RayTracingTriangle> orderedTriangles;

        BvhBuildResult(ArrayList<NodeData> nodes, ArrayList<RayTracingTriangle> orderedTriangles) {
            this.nodes = nodes;
            this.orderedTriangles = orderedTriangles;
        }
    }

    public static class NodeData {
        public RayTracingBounds bounds;
        public int triangleIndex;
        public int triangleSize;
        public int childAIndex;
        public int childBIndex;
    }

    private static class BvhNode {
        final RayTracingBounds bounds = new RayTracingBounds();
        final ArrayList<RayTracingTriangle> triangles = new ArrayList<>();
        BvhNode childA;
        BvhNode childB;

        BvhNode(ArrayList<RayTracingTriangle> sourceTriangles, int depth) {
            if (sourceTriangles == null || sourceTriangles.isEmpty()) {
                bounds.include(new Vector3(0.0f));
                return;
            }

            for (RayTracingTriangle triangle : sourceTriangles) {
                bounds.include(triangle);
            }

            if (depth == 0 || sourceTriangles.size() <= MAX_TRIANGLES_PER_LEAF) {
                triangles.addAll(sourceTriangles);
                return;
            }

            SplitResult split = findSahSplit(sourceTriangles);
            ArrayList<RayTracingTriangle> left = split.left;
            ArrayList<RayTracingTriangle> right = split.right;

            if (left.isEmpty() || right.isEmpty()) {
                triangles.addAll(sourceTriangles);
                return;
            }

            childA = new BvhNode(left, depth - 1);
            childB = new BvhNode(right, depth - 1);
        }

        private SplitResult findSahSplit(ArrayList<RayTracingTriangle> sourceTriangles) {
            RayTracingBounds centroidBounds = new RayTracingBounds();
            for (RayTracingTriangle triangle : sourceTriangles) {
                centroidBounds.include(triangle.center());
            }

            SplitChoice bestChoice = null;
            for (int axis = 0; axis < 3; axis++) {
                SplitChoice choice = findSahSplitForAxis(sourceTriangles, centroidBounds, axis);
                if (choice != null && (bestChoice == null || choice.cost < bestChoice.cost)) {
                    bestChoice = choice;
                }
            }

            if (bestChoice == null) {
                return medianSplit(sourceTriangles, largestAxis(centroidBounds.size()));
            }

            ArrayList<RayTracingTriangle> left = new ArrayList<>();
            ArrayList<RayTracingTriangle> right = new ArrayList<>();
            float axisMin = component(centroidBounds.min, bestChoice.axis);
            float axisExtent = component(centroidBounds.size(), bestChoice.axis);

            for (RayTracingTriangle triangle : sourceTriangles) {
                int binIndex = centroidBinIndex(triangle.center(), bestChoice.axis, axisMin, axisExtent);
                if (binIndex <= bestChoice.binIndex) {
                    left.add(triangle);
                } else {
                    right.add(triangle);
                }
            }

            if (left.isEmpty() || right.isEmpty()) {
                return medianSplit(sourceTriangles, bestChoice.axis);
            }
            return new SplitResult(left, right);
        }

        private SplitChoice findSahSplitForAxis(
                ArrayList<RayTracingTriangle> sourceTriangles,
                RayTracingBounds centroidBounds,
                int axis
        ) {
            float axisMin = component(centroidBounds.min, axis);
            float axisExtent = component(centroidBounds.size(), axis);
            if (axisExtent <= 0.000001f) {
                return null;
            }

            Bin[] bins = new Bin[SAH_BIN_COUNT];
            for (int i = 0; i < bins.length; i++) {
                bins[i] = new Bin();
            }

            for (RayTracingTriangle triangle : sourceTriangles) {
                int binIndex = centroidBinIndex(triangle.center(), axis, axisMin, axisExtent);
                bins[binIndex].include(triangle);
            }

            RayTracingBounds[] leftBounds = new RayTracingBounds[SAH_BIN_COUNT - 1];
            RayTracingBounds[] rightBounds = new RayTracingBounds[SAH_BIN_COUNT - 1];
            int[] leftCounts = new int[SAH_BIN_COUNT - 1];
            int[] rightCounts = new int[SAH_BIN_COUNT - 1];

            RayTracingBounds runningBounds = new RayTracingBounds();
            int runningCount = 0;
            for (int i = 0; i < SAH_BIN_COUNT - 1; i++) {
                runningBounds.include(bins[i].bounds);
                runningCount += bins[i].count;
                leftBounds[i] = copyBounds(runningBounds);
                leftCounts[i] = runningCount;
            }

            runningBounds = new RayTracingBounds();
            runningCount = 0;
            for (int i = SAH_BIN_COUNT - 1; i > 0; i--) {
                runningBounds.include(bins[i].bounds);
                runningCount += bins[i].count;
                rightBounds[i - 1] = copyBounds(runningBounds);
                rightCounts[i - 1] = runningCount;
            }

            SplitChoice best = null;
            for (int i = 0; i < SAH_BIN_COUNT - 1; i++) {
                if (leftCounts[i] == 0 || rightCounts[i] == 0) {
                    continue;
                }

                float cost = surfaceArea(leftBounds[i]) * leftCounts[i]
                        + surfaceArea(rightBounds[i]) * rightCounts[i];
                if (best == null || cost < best.cost) {
                    best = new SplitChoice(axis, i, cost);
                }
            }

            return best;
        }

        private SplitResult medianSplit(ArrayList<RayTracingTriangle> sourceTriangles, int axis) {
            ArrayList<RayTracingTriangle> sorted = new ArrayList<>(sourceTriangles);
            sorted.sort((a, b) -> Float.compare(component(a.center(), axis), component(b.center(), axis)));

            int splitIndex = sorted.size() / 2;
            ArrayList<RayTracingTriangle> left = new ArrayList<>(sorted.subList(0, splitIndex));
            ArrayList<RayTracingTriangle> right = new ArrayList<>(sorted.subList(splitIndex, sorted.size()));
            return new SplitResult(left, right);
        }

        private static int centroidBinIndex(Vector3 center, int axis, float axisMin, float axisExtent) {
            float normalized = (component(center, axis) - axisMin) / axisExtent;
            int binIndex = (int) (normalized * SAH_BIN_COUNT);
            return Math.max(0, Math.min(SAH_BIN_COUNT - 1, binIndex));
        }

        private static RayTracingBounds copyBounds(RayTracingBounds source) {
            RayTracingBounds copy = new RayTracingBounds();
            copy.include(source.min);
            copy.include(source.max);
            return copy;
        }

        private static float surfaceArea(RayTracingBounds bounds) {
            Vector3 size = bounds.size();
            float x = Math.max(size.x, 0.0f);
            float y = Math.max(size.y, 0.0f);
            float z = Math.max(size.z, 0.0f);
            return 2.0f * (x * y + y * z + z * x);
        }

        private static int largestAxis(Vector3 size) {
            if (size.x > size.y && size.x > size.z) {
                return 0;
            }
            return size.y > size.z ? 1 : 2;
        }

        private static float component(Vector3 value, int axis) {
            if (axis == 0) {
                return value.x;
            }
            return axis == 1 ? value.y : value.z;
        }

        private static class Bin {
            final RayTracingBounds bounds = new RayTracingBounds();
            int count;

            void include(RayTracingTriangle triangle) {
                bounds.include(triangle);
                count++;
            }
        }

        private static class SplitChoice {
            final int axis;
            final int binIndex;
            final float cost;

            SplitChoice(int axis, int binIndex, float cost) {
                this.axis = axis;
                this.binIndex = binIndex;
                this.cost = cost;
            }
        }

        private static class SplitResult {
            final ArrayList<RayTracingTriangle> left;
            final ArrayList<RayTracingTriangle> right;

            SplitResult(ArrayList<RayTracingTriangle> left, ArrayList<RayTracingTriangle> right) {
                this.left = left;
                this.right = right;
            }
        }
    }
}
