package dev.chililisoup.modularsynths.client.reg;

import dev.chililisoup.modularsynths.client.renderer.AbstractSynthModuleRenderer;
import dev.chililisoup.modularsynths.client.renderer.modules.DialRenderer;
import dev.chililisoup.modularsynths.client.renderer.modules.NoteSupplierRenderer;
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
        RENDERERS.put(ModBlocks.DIAL, new DialRenderer());
        RENDERERS.put(ModBlocks.NOTE_SUPPLIER, new NoteSupplierRenderer());
    }

    public static void init() {}
}
