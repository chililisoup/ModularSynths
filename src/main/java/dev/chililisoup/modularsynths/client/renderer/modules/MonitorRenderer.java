package dev.chililisoup.modularsynths.client.renderer.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.chililisoup.modularsynths.block.MonitorBlock;
import dev.chililisoup.modularsynths.block.state.MonitorDisplay;
import dev.chililisoup.modularsynths.client.renderer.AbstractSynthModuleRenderer;
import dev.chililisoup.modularsynths.client.renderer.SynthModuleRenderState;
import dev.chililisoup.modularsynths.synthesis.modules.MonitorSynth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.FrontAndTop;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.function.BiFunction;

public class MonitorRenderer extends AbstractSynthModuleRenderer<MonitorSynth, MonitorRenderer.MonitorRenderState> {
    public MonitorRenderer() {
        super(MonitorSynth.class);
    }

    @Override
    protected void submit(
            MonitorRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            BlockEntityRendererProvider.Context context
    ) {
        if (state.storedSamples == null) return;

        poseStack.pushPose();
        transformPoseStackToFront(poseStack, state.orientation, 0.5125);
        poseStack.translate(0F, 0.15625F, 0F);
        poseStack.scale(0.75F, 0.4375F, 1F);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.LINES,
                (pose, buffer) -> submitLines(state, pose, buffer)
        );

        poseStack.popPose();
    }

    private static void submitLines(MonitorRenderState state, PoseStack.Pose pose, VertexConsumer buffer) {
        BiFunction<MonitorRenderState, Integer, Vector3f> vertexPosFunction =
                state.display == MonitorDisplay.STRAIGHT ?
                        MonitorRenderer::getLineVertexPos :
                        MonitorRenderer::getCircularLineVertexPos;

        Vector3f lastPos = vertexPosFunction.apply(state, 0);
        for (int i = 1; i <= state.period && i + state.phase < state.storedSamples.length; i++) {
            submitLineVertex(state, pose, buffer, lastPos);
            lastPos = vertexPosFunction.apply(state, i);
            submitLineVertex(state, pose, buffer, lastPos);
        }
    }

    private static void submitLineVertex(MonitorRenderState state, PoseStack.Pose pose, VertexConsumer buffer, Vector3f pos) {
        buffer.addVertex(pose, pos)
                .setColor(0xFF00FF00)
                .setNormal(state.norm.x, state.norm.y, state.norm.z)
                .setLineWidth(state.lineWidth);
    }

    private static Vector3f getLineVertexPos(MonitorRenderState state, int index) {
        return new Vector3f(
                0.5F - ((float) index / state.period),
                (float) Mth.clamp((state.storedSamples[index + state.phase] + 1) / 2, 0, 1) - 0.5F,
                0
        );
    }

    private static Vector3f getCircularLineVertexPos(MonitorRenderState state, int index) {
        float offset = (Mth.clamp((float) state.storedSamples[index + state.phase], -1,  1) + 1) / 4F;
        float pos = ((float) index / state.period) * Mth.TWO_PI;
        return new Vector3f(
                0 - (offset * Mth.cos(pos)),
                (offset * Mth.sin(pos)),
                0
        );
    }

    @Override
    protected void extractRenderState(MonitorSynth synth, MonitorRenderState state, float partialTicks, FrontAndTop orientation) {
        super.extractRenderState(synth, state, partialTicks, orientation);
        state.storedSamples = synth.storedSamples();
        if (state.storedSamples == null) return;

        state.period = (int) Math.round(synth.period());
        state.phase = (int) Math.round(synth.phase() % state.storedSamples.length);
        state.norm = orientation.front().getUnitVec3().toVector3f();
        state.lineWidth = Math.max(Minecraft.getInstance().getWindow().getHeight() / 300F, 1F);
        state.display = synth.synthBlockEntity.getBlockState().getValueOrElse(MonitorBlock.DISPLAY, MonitorDisplay.STRAIGHT);
    }

    @Override
    protected MonitorRenderState createRenderState() {
        return new MonitorRenderState();
    }

    public static class MonitorRenderState extends SynthModuleRenderState {
        public double[] storedSamples;
        public int phase;
        public int period;
        public Vector3f norm;
        public float lineWidth;
        public MonitorDisplay display;
    }
}
