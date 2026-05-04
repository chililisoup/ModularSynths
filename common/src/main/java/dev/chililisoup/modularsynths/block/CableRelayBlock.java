package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class CableRelayBlock extends SynthBlock {
    public CableRelayBlock(Properties properties) {
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
        return inputStackSet[outputPort];
    }
}
