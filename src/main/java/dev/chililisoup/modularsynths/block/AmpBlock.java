package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.AmpSynth;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class AmpBlock extends AbstractSynthBlock<AmpSynth> {
    private static final MapCodec<AmpBlock> CODEC = simpleCodec(AmpBlock::new);
    private static final Vec2[] INPUT_POSITIONS = new Vec2[]{
            new Vec2(13F / 16F, 11F / 16F),
            new Vec2(13F / 16F, 5F / 16F)
    };
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{new Vec2(3F / 16F, 8F / 16F)};

    @Override
    protected @NonNull MapCodec<AmpBlock> codec() {
        return CODEC;
    }

    public AmpBlock(Properties properties) {
        super(properties, AmpSynth.class);
    }

    @Override
    public Vec2[] inputPositions() {
        return INPUT_POSITIONS;
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITION;
    }

    @Override
    public @NonNull AmpSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new AmpSynth(synthBlockEntity);
    }
}
