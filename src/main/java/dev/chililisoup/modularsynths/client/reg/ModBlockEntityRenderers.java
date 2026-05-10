package dev.chililisoup.modularsynths.client.reg;

import dev.chililisoup.modularsynths.client.renderer.SynthRenderer;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Environment(EnvType.CLIENT)
public final class ModBlockEntityRenderers {
    static {
        BlockEntityRenderers.register(ModBlockEntityTypes.SYNTH, SynthRenderer::new);
    }

    public static void init() {}
}
