package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class BasicFilterBlock extends SynthBlock { // Placeholder, rewrite to use FFT
    private final boolean isHighPass;

    public BasicFilterBlock(Properties properties, boolean isHighPass) {
        super(properties);
        this.isHighPass = isHighPass;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] output = super.requestData(inputStack, outputDirection, size, state, blockEntity);

        double lastPoint = blockEntity.getSamplePosition();
        for (int i = 0; i < size; i++) {
            double currentPoint = output[i];

            output[i] = (currentPoint + lastPoint) / 2;
            if (isHighPass) output[i] = currentPoint - output[i];

            lastPoint = currentPoint;
        }
        blockEntity.setSamplePosition(lastPoint);

        return output;
    }

    @Override
    public Direction[] getOutputs(BlockState state) {
        return new Direction[]{ state.getValue(FACING).getCounterClockWise() };
    }

    @Override
    public Direction[] getInputs(BlockState state) {
        return new Direction[]{ state.getValue(FACING).getClockWise() };
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
