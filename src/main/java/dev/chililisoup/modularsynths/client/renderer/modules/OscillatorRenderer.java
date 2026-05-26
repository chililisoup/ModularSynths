package dev.chililisoup.modularsynths.client.renderer.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.chililisoup.modularsynths.client.renderer.AbstractSynthModuleRenderer;
import dev.chililisoup.modularsynths.client.renderer.SynthModuleRenderState;
import dev.chililisoup.modularsynths.synthesis.modules.OscillatorSynth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.FrontAndTop;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class OscillatorRenderer extends AbstractSynthModuleRenderer<OscillatorSynth, OscillatorRenderer.OscillatorRenderState> {
    public OscillatorRenderer() {
        super(OscillatorSynth.class);
    }

    @Override
    protected void submit(
            OscillatorRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            BlockEntityRendererProvider.Context context
    ) {
        if (state.wave == null) return;

        poseStack.pushPose();
        transformPoseStackToFront(poseStack, state.orientation, 0.5125);
        poseStack.translate(0F, 0.15625F, 0F);
        poseStack.scale(0.75F, 0.4375F, 1F);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.LINES,
                (pose, buffer) -> submitLines(state, pose, buffer)
        );

        poseStack.translate(0, 0, 0.01F);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugQuads(),
                (pose, buffer) -> submitHarmonics(state, pose, buffer)
        );

        poseStack.popPose();
    }

    private static void submitHarmonics(OscillatorRenderState state, PoseStack.Pose pose, VertexConsumer buffer) {
        float width = 1F / state.harmonics.length;
        float padding = 0.01F;
        float paddedWidth = width - 2 * padding;

        for (int i = 0; i < state.harmonics.length; i++) {
            float x = 0.5F - (i * width + padding) - paddedWidth;
            submitQuad(pose, buffer, x, paddedWidth, state.harmonics[i]);
        }
    }

    private static void submitQuad(PoseStack.Pose pose, VertexConsumer buffer, float x, float width, float height) {
        buffer.addVertex(pose, x, -0.5F, 0).setColor(0xFF0000FF);
        buffer.addVertex(pose, x + width, -0.5F, 0).setColor(0xFF0000FF);
        buffer.addVertex(pose, x + width, height - 0.5F, 0).setColor(0xFF0000FF);
        buffer.addVertex(pose, x, height - 0.5F, 0).setColor(0xFF0000FF);
    }

    private static void submitLines(OscillatorRenderState state, PoseStack.Pose pose, VertexConsumer buffer) {
        Vector3f lastPos = getLineVertexPos(state, 0);
        for (int i = 1; i < state.wave.length; i++) {
            submitLineVertex(state, pose, buffer, lastPos);
            lastPos = getLineVertexPos(state, i);
            submitLineVertex(state, pose, buffer, lastPos);
        }
    }

    private static void submitLineVertex(OscillatorRenderState state, PoseStack.Pose pose, VertexConsumer buffer, Vector3f pos) {
        buffer.addVertex(pose, pos)
                .setColor(0xFF00FF00)
                .setNormal(state.norm.x, state.norm.y, state.norm.z)
                .setLineWidth(state.lineWidth);
    }

    private static Vector3f getLineVertexPos(OscillatorRenderState state, int index) {
        return new Vector3f(
                0.5F - (index / (state.wave.length - 1F)),
                (float) Mth.clamp((state.wave[index] + 1) / 2, 0, 1) - 0.5F,
                0
        );
    }

    @Override
    protected void extractRenderState(OscillatorSynth synth, OscillatorRenderState state, float partialTicks, FrontAndTop orientation) {
        super.extractRenderState(synth, state, partialTicks, orientation);
        state.harmonics = synth.getHarmonics();
        state.wave = synth.getWavePreview();
        state.norm = orientation.front().getUnitVec3().toVector3f();
        state.lineWidth = Math.max(Minecraft.getInstance().getWindow().getHeight() / 300F, 1F);
    }

    @Override
    protected OscillatorRenderState createRenderState() {
        return new OscillatorRenderState();
    }

    public static class OscillatorRenderState extends SynthModuleRenderState {
        public float[] harmonics;
        public double[] wave;
        public Vector3f norm;
        public float lineWidth;
    }
}
