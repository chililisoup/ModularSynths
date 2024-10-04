package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.util.EffectType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
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
    public short[] requestData(HashMap<String, short[]> inputStack, int size, BlockState state) {
        short[] outputStack = new short[size];
        short[] controlStack = new short[size];

        inputStack.forEach((direction, dataStack) -> {
            if (direction.equals("self")) return;

            short[] usedStack = Direction.byName(direction) == state.getValue(FACING).getOpposite() ? outputStack : controlStack;

            for (int i = 0; i < size; i++) {
                usedStack[i] = (short) Mth.clamp((int) dataStack[i] + (int) usedStack[i], Short.MIN_VALUE, Short.MAX_VALUE);
            }
        });

        return type.apply(outputStack, controlStack);
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
