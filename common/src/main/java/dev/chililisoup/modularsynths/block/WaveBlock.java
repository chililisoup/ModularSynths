package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import dev.chililisoup.modularsynths.util.WaveType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class WaveBlock extends SynthBlock {
    private final WaveType type;
    private int samplePosition = 0;

    public WaveBlock(Properties properties, WaveType type) {
        super(properties);
        this.type = type;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public short[] requestData(HashMap<String, short[]> inputStack, int size, BlockState state) {
        short[] outputStack = super.requestData(inputStack, size, state);

        for (int i = 0; i < size; i++) {
            outputStack[i] = type.apply(i + samplePosition, SynthesisFunctions.getFrequencyFromShort(outputStack[i]), 0.33);
        }
        this.samplePosition += size;

        return outputStack;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public short inputFallback() {
        return 4386; // short for note 72, A @ 440 Hz
    }

    @Override
    public Direction[] getOutputs(BlockState state) {
        return new Direction[]{state.getValue(FACING)};
    }

    @Override
    public Direction[] getInputs(BlockState state) {
        return new Direction[]{state.getValue(FACING).getOpposite()};
    }

    @Override
    public boolean sendsOutput() {
        return true;
    }

    @Override
    public boolean acceptsInput() {
        return true;
    }
}
