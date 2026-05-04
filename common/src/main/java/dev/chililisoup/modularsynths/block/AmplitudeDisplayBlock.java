package dev.chililisoup.modularsynths.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.renderer.SynthBlockRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;

public class AmplitudeDisplayBlock extends SynthBlock implements SynthMonitor {
    public AmplitudeDisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(8F / 16F, 4F / 16F)
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void monitorInputData(double[][] inputStackSet, int size, BlockState state, SynthBlockEntity blockEntity) {
        double totalAmplitude = 0.0;

        for (double point : inputStackSet[0]) {
            totalAmplitude += Math.abs(point);
        }

        blockEntity.setSamplePosition(totalAmplitude / size);
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
        FrontAndTop fat = blockEntity.getBlockState().getValue(BlockStateProperties.ORIENTATION);

        poseStack.pushPose();
        SynthBlockRenderer.transformPoseStackToFront(poseStack, fat);
        poseStack.scale(-0.02F, -0.02F, -0.02F);

        String text = String.format("%.1f%%", blockEntity.getSamplePosition() * 100.0);

        Font font = context.getFont();
        font.drawInBatch(
                text,
                (float)(-font.width(text) / 2),
                -12.0F,
                16777215,
                true,
                poseStack.last().pose(),
                buffer,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                LevelRenderer.getLightColor(level, blockEntity.getBlockPos().relative(fat.front()))
        );

        poseStack.popPose();
    }
}
