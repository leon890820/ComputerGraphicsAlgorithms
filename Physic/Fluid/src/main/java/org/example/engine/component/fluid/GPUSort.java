package org.example.engine.component;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.ComputeHelper;
import org.example.engine.gl.ComputeShader;

public class GPUSort {
    public static final int ENTRY_STRIDE = 4 * Integer.BYTES;

    private final ComputeShader sortShader;
    private final ComputeShader calculateOffsetsShader;

    private ComputeBuffer indexBuffer;
    private ComputeBuffer offsetBuffer;

    public GPUSort() {
        sortShader = new ComputeShader("/shaders/sort/bitonic_sort.comp");
        calculateOffsetsShader = new ComputeShader("/shaders/sort/calculate_offsets.comp");
    }

    public void setBuffers(ComputeBuffer indexBuffer, ComputeBuffer offsetBuffer) {
        this.indexBuffer = indexBuffer;
        this.offsetBuffer = offsetBuffer;
    }

    public void sort() {
        if (indexBuffer == null || indexBuffer.getCount() <= 0) {
            return;
        }

        int numEntries = indexBuffer.getCount();
        int paddedEntryCount = nextPowerOfTwo(numEntries);
        int numStages = log2(paddedEntryCount);

        sortShader.bind();
        indexBuffer.bindBase(0);
        sortShader.setInt("numEntries", numEntries);

        for (int stageIndex = 0; stageIndex < numStages; stageIndex++) {
            for (int stepIndex = 0; stepIndex < stageIndex + 1; stepIndex++) {
                int groupWidth = 1 << (stageIndex - stepIndex);
                int groupHeight = 2 * groupWidth - 1;

                sortShader.setInt("groupWidth", groupWidth);
                sortShader.setInt("groupHeight", groupHeight);
                sortShader.setInt("stepIndex", stepIndex);

                ComputeHelper.dispatch(sortShader, paddedEntryCount / 2);
                ComputeHelper.memoryBarrier();
            }
        }

        sortShader.unbind();
    }

    public void sortAndCalculateOffsets() {
        sort();
        calculateOffsets();
    }

    public void calculateOffsets() {
        if (indexBuffer == null || offsetBuffer == null || indexBuffer.getCount() <= 0) {
            return;
        }

        calculateOffsetsShader.bind();
        indexBuffer.bindBase(0);
        offsetBuffer.bindBase(1);
        calculateOffsetsShader.setInt("numEntries", indexBuffer.getCount());
        ComputeHelper.dispatch(calculateOffsetsShader, indexBuffer.getCount());
        ComputeHelper.memoryBarrier();
        calculateOffsetsShader.unbind();
    }

    public void dispose() {
        sortShader.dispose();
        calculateOffsetsShader.dispose();
    }

    private static int nextPowerOfTwo(int value) {
        int result = 1;

        while (result < value) {
            result <<= 1;
        }

        return result;
    }

    private static int log2(int value) {
        int result = 0;

        while (value > 1) {
            value >>= 1;
            result++;
        }

        return result;
    }
}
