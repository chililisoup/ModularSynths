package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;

public interface SynthMonitor {
    @Environment(EnvType.CLIENT)
    void monitorInputData(double[][] inputStackSet, int size, BlockState state, SynthBlockEntity blockEntity);
}
