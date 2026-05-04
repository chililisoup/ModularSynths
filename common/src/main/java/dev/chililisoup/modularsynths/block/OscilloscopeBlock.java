package dev.chililisoup.modularsynths.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.renderer.SynthBlockRenderer;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class OscilloscopeBlock extends SynthBlock implements SynthMonitor {
    public OscilloscopeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(3F / 16F, 4F / 16F),
                new Vec2(13F / 16F, 4F / 16F)
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void monitorInputData(double[][] inputStackSet, int size, BlockState state, SynthBlockEntity blockEntity) {
        ArrayList<double[]> savedStacks = blockEntity.getSavedStacks();

        if (savedStacks.isEmpty()) savedStacks.add(inputStackSet[0]);
        else savedStacks.set(0, inputStackSet[0]);

        double frequency = SynthesisFunctions.getFrequencyFromDouble(inputStackSet[1][0]);
        double period = SynthesisFunctions.wavePeriod(frequency);
        double bufferPeriodCount = size / period;
        double bufferPeriodOffset = period * (1.0 - (bufferPeriodCount % 1.0));

        blockEntity.setSamplePosition(
                (blockEntity.getSamplePosition() + bufferPeriodOffset) % period
        );

        ArrayList<Double> doubleData = blockEntity.getCustomDoubleData();
        if (doubleData.isEmpty()) doubleData.add(0.0);
        doubleData.set(0, period);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(
            Level level,
            SynthBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            BlockEntityRendererProvider.Context context
    ) {
        if (blockEntity.getSavedStacks().isEmpty()) return;
        if (blockEntity.getCustomDoubleData().isEmpty()) return;
        double[] waveStack = blockEntity.getSavedStacks().get(0);

        poseStack.pushPose();
        SynthBlockRenderer.transformPoseStackToFront(
                poseStack,
                blockEntity.getBlockState().getValue(BlockStateProperties.ORIENTATION),
                1.95
        );
        poseStack.translate(0F, 0.15625F, 0F);
        poseStack.scale(0.75F, 0.4375F, 1F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(1));
        Matrix4f matrix = poseStack.last().pose();

        int period = (int) Math.round(blockEntity.getCustomDoubleData().get(0));
        int startPos = (int) Math.round(blockEntity.getSamplePosition());
        for (int i = 0; i <= period && i + startPos < waveStack.length; i++) {
            vertexConsumer.vertex(
                    matrix,
                    0.5F - ((float) i / period),
                    (float) Mth.clamp((waveStack[i + startPos] + 1) / 2, 0, 1) - 0.5F,
                    0
            ).color(0F, 1F, 0F, 1F).endVertex();
        }

        poseStack.popPose();
    }
}
