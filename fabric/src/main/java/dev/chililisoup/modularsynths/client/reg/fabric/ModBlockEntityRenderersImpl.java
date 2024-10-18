package dev.chililisoup.modularsynths.client.reg.fabric;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntityRenderersImpl {
    public static <T extends BlockEntity> void registerRenderer(Supplier<BlockEntityType<T>> blockEntityType, BlockEntityRendererProvider<T> blockEntityRenderer) {
        BlockEntityRenderers.register(blockEntityType.get(), blockEntityRenderer);
    }
}
