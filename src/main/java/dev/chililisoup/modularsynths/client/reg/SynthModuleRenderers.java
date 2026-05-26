package dev.chililisoup.modularsynths.client.reg;

import dev.chililisoup.modularsynths.client.renderer.AbstractSynthModuleRenderer;
import dev.chililisoup.modularsynths.client.renderer.modules.MessageSupplierRenderer;
import dev.chililisoup.modularsynths.client.renderer.modules.MonitorRenderer;
import dev.chililisoup.modularsynths.client.renderer.modules.OscillatorRenderer;
import dev.chililisoup.modularsynths.reg.ModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class SynthModuleRenderers {
    private static final HashMap<Block, AbstractSynthModuleRenderer<?, ?>> RENDERERS = new HashMap<>();

    public static @Nullable AbstractSynthModuleRenderer<?, ?> getRenderer(Block block) {
        return RENDERERS.get(block);
    }

    public static @Nullable AbstractSynthModuleRenderer<?, ?> getRenderer(BlockState blockState) {
        return getRenderer(blockState.getBlock());
    }

    static {
        RENDERERS.put(ModBlocks.DIAL, new MessageSupplierRenderer());
        RENDERERS.put(ModBlocks.NOTE_SUPPLIER, new MessageSupplierRenderer());
        RENDERERS.put(ModBlocks.NOTE_SHIFTER, new MessageSupplierRenderer());
        RENDERERS.put(ModBlocks.POLY_TO_MONO, new MessageSupplierRenderer());
        RENDERERS.put(ModBlocks.MONITOR, new MonitorRenderer());
        RENDERERS.put(ModBlocks.OSCILLATOR, new OscillatorRenderer());
    }

    public static void init() {}
}
