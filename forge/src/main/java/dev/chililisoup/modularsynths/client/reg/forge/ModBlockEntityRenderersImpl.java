package dev.chililisoup.modularsynths.client.reg.forge;

import dev.chililisoup.modularsynths.forge.ModularSynthsForge;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntityRenderersImpl {
    public static <T extends BlockEntity> void registerRenderer(Supplier<BlockEntityType<T>> blockEntityType, BlockEntityRendererProvider<T> blockEntityRenderer) {
        ModularSynthsForge.registerBlockEntityRenderer(blockEntityType, blockEntityRenderer);
    }
}
