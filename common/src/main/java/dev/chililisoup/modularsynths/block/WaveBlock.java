package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import dev.chililisoup.modularsynths.util.WaveType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;

public class WaveBlock extends SynthBlock {
    private final WaveType type;

    public WaveBlock(Properties properties, WaveType type) {
        super(properties);
        this.type = type;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[][] requestPolyData(HashMap<String, double[][]> inputStackSet, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[][] outputStackSet = inputStackSet.values().iterator().next();
        ArrayList<Double> samplePositions = blockEntity.getCustomDoubleData();

        for (int i = 0; i < outputStackSet.length; i++) {
            double[] outputStack = outputStackSet[i];

            if (samplePositions.size() <= i) samplePositions.add(0.0);
            double samplePosition = samplePositions.get(i);

            for (int j = 0; j < size; j++) {
                double frequency = SynthesisFunctions.getFrequencyFromDouble(outputStack[j]);
                outputStack[j] = this.type.apply(samplePosition) / 4.0;
                samplePosition += SynthesisFunctions.waveStep(frequency);
            }

            samplePositions.set(i, samplePosition % 1.0);
        }

        return outputStackSet;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double inputFallback() {
        return 0.125; // double for A @ 440 Hz
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
