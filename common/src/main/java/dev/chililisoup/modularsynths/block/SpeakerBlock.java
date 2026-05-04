package dev.chililisoup.modularsynths.block;

import net.minecraft.world.phys.Vec2;

public class SpeakerBlock extends SynthBlock {
    public SpeakerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(8F / 16F, 12F / 16F)
        };
    }
}
