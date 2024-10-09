package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import dev.chililisoup.modularsynths.util.WaveType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class LfoBlock extends SynthBlock {
    public LfoBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = super.requestData(inputStack, outputDirection, size, state, blockEntity);
        double samplePosition = blockEntity.getSamplePosition();

        for (int i = 0; i < size; i++) {
            double frequency = outputStack[i] * 5.0;
            outputStack[i] = WaveType.SINE.apply(samplePosition) / 4.0;
            samplePosition += SynthesisFunctions.waveStep(frequency);
        }

        blockEntity.setSamplePosition(samplePosition % 1.0);
        return outputStack;
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
