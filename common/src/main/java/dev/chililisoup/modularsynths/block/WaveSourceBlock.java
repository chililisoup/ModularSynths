package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import dev.chililisoup.modularsynths.util.WaveType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;

public class WaveSourceBlock extends SynthBlock {
    public WaveSourceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(13F / 16F, 14F / 16F),
                new Vec2(13F / 16F, 10F / 16F),
                new Vec2(13F / 16F, 6F  / 16F),
                new Vec2(13F / 16F, 2F  / 16F)
        };
    }

    @Override
    public Vec2[] getOutputPositions() {
        return new Vec2[]{
                new Vec2(3F / 16F, 14F / 16F),
                new Vec2(3F / 16F, 10F / 16F),
                new Vec2(3F / 16F, 6F  / 16F),
                new Vec2(3F / 16F, 2F  / 16F)
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestOutputData(double[][] inputStackSet, int size, int outputPort, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = inputStackSet[outputPort];

        ArrayList<Double> samplePositions = blockEntity.getCustomDoubleData();
        while (samplePositions.size() <= outputPort) samplePositions.add(0.0);
        double samplePosition = samplePositions.get(outputPort);

        WaveType type = WaveType.values()[outputPort];

        for (int i = 0; i < size; i++) {
            double frequency = SynthesisFunctions.getFrequencyFromDouble(inputStackSet[outputPort][i]);
            outputStack[i] = type.apply(samplePosition) / 4.0;
            samplePosition += SynthesisFunctions.waveStep(frequency);
        }

        samplePositions.set(outputPort, samplePosition % 1.0);

        return outputStack;
    }
}
