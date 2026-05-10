package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.SynthRelay;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class CableRelayBlock extends SynthBlock<SynthRelay> {
    private static final MapCodec<CableRelayBlock> CODEC = simpleCodec(CableRelayBlock::new);
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
    protected @NonNull MapCodec<CableRelayBlock> codec() {
        return CODEC;
    }

    public CableRelayBlock(BlockBehaviour.Properties properties) {
        super(properties, SynthRelay.class);
    }

    @Override
    public @NonNull SynthRelay newSynth(SynthBlockEntity synthBlockEntity) {
        return new SynthRelay(synthBlockEntity, 4);
    }

    @Override
    public Vec2[] inputPositions() {
        return INPUT_POSITIONS;
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITIONS;
    }
}
