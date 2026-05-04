package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.Arrays;

public class PortamentoBlock extends SynthBlock {
    public PortamentoBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(13F / 16F, 11F / 16F),
                new Vec2(13F / 16F, 5F / 16F)
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
        double[] controlStack = inputStackSet[1];

        ArrayList<Double> customData = blockEntity.getCustomDoubleData();

        double target = customData.isEmpty() ? outputStack[0] : customData.get(1);
        double from = customData.isEmpty() ? target : customData.get(0);
        double last = customData.isEmpty() ? from : customData.get(2);

        for (int i = 0; i < size; i++) {
            if (outputStack[i] == last && last == target) continue;

            if (target != outputStack[i]) {
                from = last;
                target = outputStack[i];
            }

            double step = (target - from) / (Math.max(Math.abs(controlStack[i]), 0.01) * ModularSynths.SAMPLE_RATE);
            double oldValue = last;
            double newValue = oldValue + step;

            if ((oldValue < target && newValue >= target) || (oldValue > target && newValue <= target)) {
                newValue = target;
                from = target;
            }

            last = newValue;
            outputStack[i] = newValue;
        }

        if (customData.size() < 3) {
            customData.addAll(Arrays.asList(0.0, 0.0, 0.0));
        }

        customData.set(0, from);
        customData.set(1, target);
        customData.set(2, last);
        return outputStack;
    }
}
