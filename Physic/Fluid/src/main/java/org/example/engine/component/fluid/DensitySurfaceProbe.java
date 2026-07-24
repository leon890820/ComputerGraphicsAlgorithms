package org.example.engine.component.fluid;

import org.example.engine.component.core.Component;
import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.ComputeHelper;
import org.example.engine.gl.ComputeShader;
import org.example.engine.gl.Texture3D;
import org.example.engine.math.Vector3;

public class DensitySurfaceProbe extends Component {
    private final ComputeShader shader;
    private final ComputeBuffer resultBuffer;

    public DensitySurfaceProbe() {
        shader = new ComputeShader("/shaders/particle/compute/particle_density_surface_probe.comp");
        resultBuffer = ComputeBuffer.fromFloats(new float[]{0.0f, 0.0f, 0.0f, 0.0f}, 4 * Float.BYTES);
    }

    public float probeDistance(
            Texture3D densityTexture,
            Vector3 boundsCenter,
            Vector3 boundsSize,
            Vector3 rayOrigin,
            Vector3 rayDirection,
            float isoLevel,
            float maxDistance,
            int probeSteps,
            float fallbackDistance
    ) {
        if (densityTexture == null
                || !densityTexture.isUploaded()
                || boundsCenter == null
                || boundsSize == null
                || rayOrigin == null
                || rayDirection == null
                || maxDistance <= 0.0f) {
            return fallbackDistance;
        }

        shader.bind();
        resultBuffer.bindBase(0);
        shader.setTexture("densityTexture", densityTexture, 0);
        shader.setVector3("boundsCenter", boundsCenter);
        shader.setVector3("boundsSize", boundsSize);
        shader.setVector3("rayOrigin", rayOrigin);
        shader.setVector3("rayDirection", rayDirection);
        shader.setFloat("isoLevel", isoLevel);
        shader.setFloat("maxDistance", maxDistance);
        shader.setInt("probeSteps", Math.max(1, probeSteps));
        ComputeHelper.dispatch(shader, 1);
        ComputeHelper.memoryBarrier();
        shader.unbind();

        float[] result = resultBuffer.getFloatData(4);
        if (result.length >= 2 && result[1] > 0.5f) {
            return result[0];
        }

        return fallbackDistance;
    }

    @Override
    public void dispose() {
        shader.dispose();
        resultBuffer.dispose();
    }
}
