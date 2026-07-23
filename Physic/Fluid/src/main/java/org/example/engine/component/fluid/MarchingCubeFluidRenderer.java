package org.example.engine.component;

import org.example.engine.gl.ComputeBuffer;
import org.example.engine.gl.ComputeHelper;
import org.example.engine.gl.ComputeShader;
import org.example.engine.gl.Texture;
import org.example.engine.material.FluidMarchingGpuMaterial;
import org.example.engine.material.MaterialRenderData;
import org.example.engine.render.RenderContext;
import org.example.engine.resource.ResourceDisposalContext;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL40.glDrawArraysIndirect;
import static org.lwjgl.opengl.GL11.GL_BLEND;

public class MarchingCubeFluidRenderer {
    private static final int VOXEL_RESOLUTION = 127;
    private static final int MAX_TRIANGLES_PER_VOXEL = 5;
    private static final int TRIANGLE_STRIDE = 12 * Float.BYTES;
    private static final int DRAW_INDIRECT_STRIDE = 4 * Integer.BYTES;

    private final ComputeShader marchingShader;
    private final ComputeShader prepareIndirectShader;
    private final ComputeBuffer triangleBuffer;
    private final ComputeBuffer triangleCountBuffer;
    private final ComputeBuffer drawIndirectBuffer;
    private final FluidMarchingGpuMaterial material;
    private final int maxTriangles;
    private final int vao;

    private Texture sceneColorTexture;
    private Texture sceneDepthTexture;
    private int screenWidth = 1;
    private int screenHeight = 1;
    private float isoLevel = 10.0f;

    public MarchingCubeFluidRenderer() {
        int numVoxels = VOXEL_RESOLUTION * VOXEL_RESOLUTION * VOXEL_RESOLUTION;
        maxTriangles = numVoxels * MAX_TRIANGLES_PER_VOXEL;
        marchingShader = new ComputeShader("/shaders/particle/compute/fluid_marching_cubes.comp");
        prepareIndirectShader = new ComputeShader("/shaders/particle/compute/fluid_prepare_indirect.comp");
        triangleBuffer = new ComputeBuffer(maxTriangles, TRIANGLE_STRIDE);
        triangleCountBuffer = ComputeBuffer.fromInts(new int[]{0}, Integer.BYTES);
        drawIndirectBuffer = new ComputeBuffer(1, DRAW_INDIRECT_STRIDE, GL_DYNAMIC_DRAW);
        drawIndirectBuffer.setData(new int[]{0, 1, 0, 0});
        material = new FluidMarchingGpuMaterial();
        vao = glGenVertexArrays();
    }

    public MarchingCubeFluidRenderer setIsoLevel(float isoLevel) {
        this.isoLevel = isoLevel;
        return this;
    }

    public MarchingCubeFluidRenderer setSceneTextures(
            Texture sceneColorTexture,
            Texture sceneDepthTexture,
            int screenWidth,
            int screenHeight
    ) {
        this.sceneColorTexture = sceneColorTexture;
        this.sceneDepthTexture = sceneDepthTexture;
        this.screenWidth = Math.max(1, screenWidth);
        this.screenHeight = Math.max(1, screenHeight);
        return this;
    }

    public void render(RenderContext ctx, ParticleSimulator simulator) {
        if (ctx == null || ctx.camera == null || simulator == null) {
            return;
        }

        dispatch(simulator);
        prepareIndirectDraw();
        draw(ctx, simulator);
    }

    public void dispose(ResourceDisposalContext disposalContext) {
        if (disposalContext != null) {
            disposalContext.trackMaterial(material);
        } else {
            material.dispose();
        }

        marchingShader.dispose();
        prepareIndirectShader.dispose();
        triangleBuffer.dispose();
        triangleCountBuffer.dispose();
        drawIndirectBuffer.dispose();
        glDeleteVertexArrays(vao);
    }

    private void dispatch(ParticleSimulator simulator) {
        triangleCountBuffer.setData(new int[]{0});

        float width = simulator.getDensityBoundsSize().x;
        float height = simulator.getDensityBoundsSize().y;
        float depth = simulator.getDensityBoundsSize().z;
        float minX = simulator.getDensityBoundsCenter().x - width * 0.5f;
        float minY = simulator.getDensityBoundsCenter().y - height * 0.5f;
        float minZ = simulator.getDensityBoundsCenter().z - depth * 0.5f;

        marchingShader.bind();
        triangleBuffer.bindBase(0);
        triangleCountBuffer.bindBase(1);
        marchingShader.setTexture("densityTexture", simulator.getDensityVolumeTexture(), 0);
        marchingShader.setInt("maxTriangles", maxTriangles);
        marchingShader.setInt("numVoxelPerAxis", VOXEL_RESOLUTION, VOXEL_RESOLUTION, VOXEL_RESOLUTION);
        marchingShader.setVector3("minPos", minX, minY, minZ);
        marchingShader.setVector3(
                "size",
                width / VOXEL_RESOLUTION,
                height / VOXEL_RESOLUTION,
                depth / VOXEL_RESOLUTION
        );
        marchingShader.setFloat("isoLevel", isoLevel);
        ComputeHelper.dispatch(marchingShader, VOXEL_RESOLUTION, VOXEL_RESOLUTION, VOXEL_RESOLUTION);
        ComputeHelper.memoryBarrier();
        marchingShader.unbind();
    }

    private void prepareIndirectDraw() {
        prepareIndirectShader.bind();
        triangleCountBuffer.bindBase(0);
        drawIndirectBuffer.bindBase(1);
        prepareIndirectShader.setInt("maxTriangles", maxTriangles);
        ComputeHelper.dispatch(prepareIndirectShader, 1);
        ComputeHelper.memoryBarrier();
        prepareIndirectShader.unbind();
    }

    private void draw(RenderContext ctx, ParticleSimulator simulator) {
        material
                .setTriangleBuffer(triangleBuffer)
                .setDensityTexture(simulator.getDensityVolumeTexture())
                .setBounds(simulator.getDensityBoundsCenter(), simulator.getDensityBoundsSize())
                .setViewProjection(ctx.camera.Matrix())
                .setCameraPosition(ctx.camera.transform.position)
                .setCameraClip(ctx.camera.getNear(), ctx.camera.getFar())
                .setSceneTextures(sceneColorTexture, sceneDepthTexture, screenWidth, screenHeight);

        material.bind();
        material.run(new MaterialRenderData());

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        glBindVertexArray(vao);
        drawIndirectBuffer.bind(GL_DRAW_INDIRECT_BUFFER);
        glDrawArraysIndirect(GL_TRIANGLES, 0);
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, 0);
        glBindVertexArray(0);

        glDepthMask(true);
        glDisable(GL_BLEND);

        material.cleanup();
        material.unbind();
    }
}
