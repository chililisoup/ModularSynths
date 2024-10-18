package dev.chililisoup.modularsynths.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SynthBlockRenderer implements BlockEntityRenderer<SynthBlockEntity> {
    private final BlockEntityRendererProvider.Context context;

    public SynthBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(SynthBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        blockEntity.render(partialTick, poseStack, buffer, packedLight, packedOverlay, context);
    }
}
