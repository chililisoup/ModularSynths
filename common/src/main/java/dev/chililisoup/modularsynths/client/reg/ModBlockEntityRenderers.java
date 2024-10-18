package dev.chililisoup.modularsynths.client.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.chililisoup.modularsynths.client.renderer.SynthBlockRenderer;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntityRenderers {
    public static void init() {
        registerRenderer(ModBlockEntityTypes.SYNTH, SynthBlockRenderer::new);
    }

    @ExpectPlatform
    private static <T extends BlockEntity> void registerRenderer(Supplier<BlockEntityType<T>> blockEntityType, BlockEntityRendererProvider<T> blockEntityRenderer) {
        throw new AssertionError();
    }
}
