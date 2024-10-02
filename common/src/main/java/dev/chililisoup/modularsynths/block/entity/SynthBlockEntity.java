package dev.chililisoup.modularsynths.block.entity;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class SynthBlockEntity extends BlockEntity {
    private final ArrayList<SynthBlockEntity> inputBlocks = new ArrayList<>();

    public SynthBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SYNTH.get(), pos, blockState);
    }

    @Environment(EnvType.CLIENT)
    private short[] request(int size, int depth) {
        short[] inputStack = new short[size];

        if (depth <= ModularSynths.MAX_DEPTH) {
            this.inputBlocks.forEach(synthBlockEntity -> {
                short[] input = synthBlockEntity.request(size, depth + 1);
                for (int i = 0; i < size; i++) {
                    inputStack[i] += input[i]; // this might need to be checked to prevent rollover?
                }
            });
        }

        return ((SynthBlock) this.getBlockState().getBlock()).requestData(inputStack, this.getBlockState());
    }

    @Environment(EnvType.CLIENT)
    public short[] request(int size) {
        return this.request(size, 0);
    }

    public void setInputBlocks(ArrayList<SynthBlockEntity> inputBlocks) {
        this.inputBlocks.clear();
        this.inputBlocks.addAll(inputBlocks);
    }
}
