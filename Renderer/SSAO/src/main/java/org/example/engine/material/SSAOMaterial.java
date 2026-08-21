package org.example.engine.material;

import org.example.engine.gl.Texture;
import org.example.engine.math.Matrix4;
import org.example.engine.math.Vector3;
import org.example.engine.render.GBuffer;
import org.example.engine.render.RenderContext;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL33.*;

public class SSAOMaterial extends Material {

    private static final int KERNEL_SIZE = 64;
    private static final int NOISE_SIZE = 4;

    private Texture positionTex;
    private Texture normalTex;
    private Texture depthTex;
    private final Texture noiseTex;
    private final Vector3[] sampleKernel = new Vector3[KERNEL_SIZE];
    private Matrix4 projectionMatrix = Matrix4.Identity();
    private Matrix4 viewMatrix = Matrix4.Identity();
    private float noiseScaleX = 1.0f;
    private float noiseScaleY = 1.0f;
    private float radius = 60.0f;
    private float bias = 0.1f;
    private float power = 2.0f;

    public SSAOMaterial(String frag, String vert) {
        super(frag, vert);
        generateSampleKernel();
        noiseTex = generateNoiseTexture();
    }

    public SSAOMaterial setGBuffer(GBuffer gBuffer) {
        if (gBuffer == null) {
            positionTex = null;
            normalTex = null;
            depthTex = null;
            return this;
        }

        positionTex = gBuffer.position;
        normalTex = gBuffer.normal;
        depthTex = gBuffer.depth;
        return this;
    }

    public SSAOMaterial setRenderContext(RenderContext ctx) {
        if (ctx == null || ctx.camera == null) {
            return this;
        }

        projectionMatrix = ctx.camera.getProjectionMatrix();
        viewMatrix = ctx.camera.getViewMatrix();
        noiseScaleX = ctx.screenWidth / (float) NOISE_SIZE;
        noiseScaleY = ctx.screenHeight / (float) NOISE_SIZE;
        return this;
    }

    @Override
    public void run(MaterialRenderData data) {
        setTexture("gPosition", positionTex, 0);
        setTexture("gNormal", normalTex, 1);
        setTexture("gDepth", depthTex, 2);
        setTexture("texNoise", noiseTex, 3);

        setMatrix4ToUniform("projectionMatrix", projectionMatrix);
        setMatrix4ToUniform("viewMatrix", viewMatrix);
        setIntToUniform("kernelSize", KERNEL_SIZE);
        setFloatToUniform("radius", radius);
        setFloatToUniform("bias", bias);
        setFloatToUniform("power", power);
        setVector2ToUniform("noiseScale", noiseScaleX, noiseScaleY);

        for (int i = 0; i < KERNEL_SIZE; i++) {
            setVector3ToUniform("samples[" + i + "]", sampleKernel[i]);
        }
    }

    @Override
    public void cleanup() {
        unbindTexture(0);
        unbindTexture(1);
        unbindTexture(2);
        unbindTexture(3);
    }

    private void generateSampleKernel() {
        Random random = new Random(7);

        for (int i = 0; i < KERNEL_SIZE; i++) {
            Vector3 sample = new Vector3(
                    randomSigned(random),
                    randomSigned(random),
                    random.nextFloat()
            ).unit_vector();

            sample = sample.mult(random.nextFloat());

            float scale = i / (float) KERNEL_SIZE;
            scale = lerp(0.1f, 1.0f, scale * scale);
            sampleKernel[i] = sample.mult(scale);
        }
    }

    private Texture generateNoiseTexture() {
        Random random = new Random(13);
        ByteBuffer pixels = MemoryUtil.memAlloc(NOISE_SIZE * NOISE_SIZE * 4);

        for (int i = 0; i < NOISE_SIZE * NOISE_SIZE; i++) {
            putSignedByte(pixels, randomSigned(random));
            putSignedByte(pixels, randomSigned(random));
            putSignedByte(pixels, 0.0f);
            pixels.put((byte) 255);
        }

        pixels.flip();

        Texture texture = new Texture();
        texture.setRawRGBA(pixels, NOISE_SIZE, NOISE_SIZE);
        texture.setSamplingMode(GL_NEAREST);
        texture.setWrapMode(GL_REPEAT);

        MemoryUtil.memFree(pixels);
        return texture;
    }

    private float randomSigned(Random random) {
        return random.nextFloat() * 2.0f - 1.0f;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private void putSignedByte(ByteBuffer buffer, float value) {
        int encoded = Math.round((value * 0.5f + 0.5f) * 255.0f);
        encoded = Math.max(0, Math.min(255, encoded));
        buffer.put((byte) encoded);
    }
}
