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

public class EnvelopeBlock extends SynthBlock {
    private static final double attack = 0.05 * ModularSynths.SAMPLE_RATE;
    private static final double decay = 0.1 * ModularSynths.SAMPLE_RATE;
    private static final double sustain = 0.8;
    private static final double release = 0.2 * ModularSynths.SAMPLE_RATE;

    public EnvelopeBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[][] requestPolyData(HashMap<String, double[][]> inputStackSet, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        final int polyCount = this.getPolyCount(inputStackSet);

        double[][] outputStackSet = new double[polyCount][size];
        double[][] controlStackSet = new double[polyCount][size];

        inputStackSet.forEach((direction, dataStackSet) -> {
            if (direction.equals("self")) return;

            double[][] usedStackSet = Direction.byName(direction) == state.getValue(FACING).getOpposite() ? outputStackSet : controlStackSet;

            for (int i = 0; i < usedStackSet.length; i++) {
                for (int j = 0; j < size; j++) usedStackSet[i][j] += dataStackSet[i][j];
            }
        });

        ArrayList<Integer> envelopeData = blockEntity.getCustomIntData();
        for (int i = envelopeData.size(); i < polyCount; i++) envelopeData.add(0);

        for (int i = 0; i < polyCount; i++) {
            int envelopePosition = envelopeData.get(i);

            for (int j = 0; j < size; j++) {
                if (controlStackSet[i][j] > 0.5 && envelopePosition < 0) envelopePosition = 0;
                if (controlStackSet[i][j] > 0.5 || envelopePosition < 0) envelopePosition++;
                if (controlStackSet[i][j] < 0.5 && envelopePosition > 0) envelopePosition = (int) Math.round(-release);

                if (envelopePosition == 0) {
                    outputStackSet[i][j] = 0;
                } else if (envelopePosition < 0) {
                    outputStackSet[i][j] *= (-envelopePosition * sustain) / release;
                } else if (envelopePosition < attack) {
                    outputStackSet[i][j] *= 1.0 - ((attack - envelopePosition) / attack);
                } else if (envelopePosition < attack + decay)
                    outputStackSet[i][j] *= ((decay - envelopePosition + attack) / decay) * (1.0 - sustain) + sustain;
                else outputStackSet[i][j] *= sustain;
            }

            envelopeData.set(i, envelopePosition);
        }

        return outputStackSet;
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
