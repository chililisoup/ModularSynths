package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.EffectType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.HashMap;

public class EffectBlock extends SynthBlock {
    private final EffectType type;

    public EffectBlock(Properties properties, EffectType type) {
        super(properties);
        this.type = type;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[][] requestPolyData(HashMap<String, double[][]> inputStackSet, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        final int polyCount = this.getPolyCount(inputStackSet);

        double[][] outputStackSet = new double[polyCount][size];
        double[][] controlStackSet = new double[polyCount][size];

        inputStackSet.forEach((direction, dataStackSet) -> {
            if (direction.equals("self")) return;

            boolean useOutput = Direction.byName(direction) == state.getValue(FACING).getOpposite();
            double[][] usedStackSet = useOutput ? outputStackSet : controlStackSet;

            for (int i = 0; i < dataStackSet.length; i++) {
                for (int j = 0; j < size; j++) usedStackSet[i][j] += dataStackSet[i][j];
            }

            if (!useOutput && dataStackSet.length < polyCount) {
                for (int i = dataStackSet.length; i < polyCount; i++) {
                    usedStackSet[i] = usedStackSet[0];
                }
            }
        });

        for (int i = 0; i < polyCount; i++) {
            type.apply(outputStackSet[i], controlStackSet[i]);
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
