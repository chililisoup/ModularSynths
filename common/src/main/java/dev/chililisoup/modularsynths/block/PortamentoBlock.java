package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PortamentoBlock extends SynthBlock {
    public PortamentoBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = new double[size];
        double[] controlStack = new double[size];

        inputStack.forEach((direction, dataStack) -> {
            if (direction.equals("self")) return;

            double[] usedStack = Direction.byName(direction) == state.getValue(FACING).getOpposite() ? outputStack : controlStack;

            for (int i = 0; i < size; i++) {
                usedStack[i] = dataStack[i] + usedStack[i];
            }
        });

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

            double step = (target - from) / (Math.abs(controlStack[i]) * ModularSynths.SAMPLE_RATE);
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

    @Override
    public Direction[] getOutputs(BlockState state) {
        return new Direction[]{state.getValue(FACING)};
    }

    @Override
    public Direction[] getInputs(BlockState state) {
        return Arrays.stream(Direction.values()).filter(
                direction -> direction != state.getValue(FACING)
        ).toList().toArray(new Direction[0]);
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
