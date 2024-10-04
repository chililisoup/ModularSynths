package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import dev.chililisoup.modularsynths.util.WaveType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class LfoBlock extends SynthBlock {
    private int samplePosition = 0;

    public LfoBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public short[] requestData(HashMap<String, short[]> inputStack, int size, BlockState state) {
        short[] outputStack = super.requestData(inputStack, size, state);

        for (int i = 0; i < size; i++) {
            outputStack[i] = WaveType.SINE.apply(i + samplePosition, (((double) outputStack[i] + 32768.0) / 65535.0) * 5.0, 1);
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
