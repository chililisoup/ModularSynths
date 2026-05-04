package dev.chililisoup.modularsynths.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntityOld;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SynthBlockOldRenderer implements BlockEntityRenderer<SynthBlockEntityOld> {
    private final BlockEntityRendererProvider.Context context;

    public SynthBlockOldRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(SynthBlockEntityOld blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        blockEntity.render(partialTick, poseStack, buffer, packedLight, packedOverlay, context);
    }
}
