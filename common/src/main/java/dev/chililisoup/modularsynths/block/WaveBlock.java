package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import dev.chililisoup.modularsynths.util.WaveType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class WaveBlock extends SynthBlock {
    private final WaveType type;
    private int samplePosition = 0;

    public WaveBlock(Properties properties, WaveType type) {
        super(properties);
        this.type = type;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public short[] requestData(short[] data, BlockState state) {
        for (int i = 0; i < data.length; i++) {
            data[i] = type.apply(i + samplePosition, SynthesisFunctions.getFrequencyFromShort(data[i]), 0.33);
        }
        this.samplePosition += data.length;

        return data;
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
