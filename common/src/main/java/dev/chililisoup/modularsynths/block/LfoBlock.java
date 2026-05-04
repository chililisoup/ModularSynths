package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntityOld;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import dev.chililisoup.modularsynths.util.WaveType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.HashMap;

public class LfoBlock extends SynthBlock {
    public LfoBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(13F / 16F, 8F / 16F)
        };
    }

    @Override
    public Vec2[] getOutputPositions() {
        return new Vec2[]{
                new Vec2(3F / 16F, 8F / 16F),
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestOutputData(double[][] inputStackSet, int size, int outputPort, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = inputStackSet[0];
        double samplePosition = blockEntity.getSamplePosition();

        for (int i = 0; i < size; i++) {
            double frequency = outputStack[i] * 5.0;
            outputStack[i] = (WaveType.SINE.apply(samplePosition) + 1.0) / 2.0;
            samplePosition += SynthesisFunctions.waveStep(frequency);
        }

        blockEntity.setSamplePosition(samplePosition % 1.0);
        return outputStack;
    }
}
