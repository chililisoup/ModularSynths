package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.EffectType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class EffectBlock extends SynthBlock {
    private final EffectType type;

    public EffectBlock(Properties properties, EffectType type) {
        super(properties);
        this.type = type;
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
        return type.apply(inputStackSet[0], inputStackSet[1]);
    }
}
