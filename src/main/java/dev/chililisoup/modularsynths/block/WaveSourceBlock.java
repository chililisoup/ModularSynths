package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.WaveSourceSynth;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class WaveSourceBlock extends AbstractSynthBlock<WaveSourceSynth> {
    private static final MapCodec<WaveSourceBlock> CODEC = simpleCodec(WaveSourceBlock::new);
    private static final Vec2[] INPUT_POSITIONS = new Vec2[]{
            new Vec2(13F / 16F, 14F / 16F),
            new Vec2(13F / 16F, 10F / 16F),
            new Vec2(13F / 16F, 6F  / 16F),
            new Vec2(13F / 16F, 2F  / 16F)
    };
    private static final Vec2[] OUTPUT_POSITIONS = new Vec2[]{
            new Vec2(3F / 16F, 14F / 16F),
            new Vec2(3F / 16F, 10F / 16F),
            new Vec2(3F / 16F, 6F  / 16F),
            new Vec2(3F / 16F, 2F  / 16F)
    };

    @Override
    protected @NonNull MapCodec<WaveSourceBlock> codec() {
        return CODEC;
    }

    public WaveSourceBlock(Properties properties) {
        super(properties, WaveSourceSynth.class);
    }

    @Override
    public Vec2[] inputPositions() {
        return INPUT_POSITIONS;
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITIONS;
    }

    @Override
    public @NonNull WaveSourceSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new WaveSourceSynth(synthBlockEntity);
    }
}
