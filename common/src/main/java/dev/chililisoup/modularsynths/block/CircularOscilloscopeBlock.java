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

public class CircularOscilloscopeBlock extends OscilloscopeBlock {
    public CircularOscilloscopeBlock(Properties properties) {
        super(properties);
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
        poseStack.scale(0.4375F, 0.4375F, 1F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(1));
        Matrix4f matrix = poseStack.last().pose();

        int period = (int) Math.round(blockEntity.getCustomDoubleData().get(0));
        int startPos = (int) Math.round(blockEntity.getSamplePosition());
        for (int i = 0; i <= period && i + startPos < waveStack.length; i++) {
            float offset = (Mth.clamp((float) waveStack[i + startPos], -1F,  1F) + 1F) / 4F;
            float pos = ((float) i / period) * Mth.TWO_PI;

            vertexConsumer.vertex(
                    matrix,
                    0F - (offset * Mth.cos(pos)),
                    (offset * Mth.sin(pos)),
                    0
            ).color(0F, 1F, 0F, 1F).endVertex();
        }

        poseStack.popPose();
    }
}
