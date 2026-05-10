package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.SinSynth;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class SinBlock extends SynthBlock<SinSynth> {
    private static final MapCodec<SinBlock> CODEC = simpleCodec(SinBlock::new);
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{
            new Vec2(8F / 16F, 12F / 16F)
    };

    @Override
    protected @NonNull MapCodec<SinBlock> codec() {
        return CODEC;
    }

    public SinBlock(Properties properties) {
        super(properties, SinSynth.class);
    }

    @Override
    public @NonNull SinSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new SinSynth(synthBlockEntity);
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITION;
    }
}
