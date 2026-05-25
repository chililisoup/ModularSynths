package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.SamplerSynth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class SamplerSynthBlock extends AbstractSynthBlock<SamplerSynth> {
    private static final MapCodec<SamplerSynthBlock> CODEC = simpleCodec(SamplerSynthBlock::new);
    private static final Vec2[] INPUT_POSITIONS = new Vec2[]{
            new Vec2(13F / 16F, 11F / 16F),
            new Vec2(13F / 16F, 5F / 16F)
    };
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{new Vec2(3F / 16F, 8F / 16F)};

    @Override
    protected @NonNull MapCodec<SamplerSynthBlock> codec() {
        return CODEC;
    }

    public SamplerSynthBlock(Properties properties) {
        super(properties, SamplerSynth.class);
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
    public @NonNull SamplerSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new SamplerSynth(synthBlockEntity);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hitResult
    ) {
        FrontAndTop orientation = state.getValueOrElse(ORIENTATION, FrontAndTop.NORTH_UP);
        if (hitResult.getDirection() != orientation.front())
            return InteractionResult.PASS;

        if (!(this.getBlockEntity(level, pos) instanceof SynthBlockEntity synthBlockEntity))
            return InteractionResult.PASS;

        if (getPortHit(state, orientation, hitResult).isPresent())
            return InteractionResult.PASS;

        player.modularSynths$openSynthScreen(synthBlockEntity);
        return InteractionResult.SUCCESS;
    }
}
