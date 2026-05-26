package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.OscillatorSynth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class OscillatorBlock extends AbstractSynthBlock<OscillatorSynth> {
    private static final MapCodec<OscillatorBlock> CODEC = simpleCodec(OscillatorBlock::new);
    private static final Vec2[] INPUT_POSITIONS = new Vec2[]{
            new Vec2(8F / 16F, 4F / 16F),
            new Vec2(13F / 16F, 4F / 16F)
    };
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{new Vec2(3F / 16F, 4F / 16F)};

    @Override
    protected @NonNull MapCodec<OscillatorBlock> codec() {
        return CODEC;
    }

    public OscillatorBlock(Properties properties) {
        super(properties, OscillatorSynth.class);
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
    public @NonNull OscillatorSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new OscillatorSynth(synthBlockEntity);
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

        if (!(synthBlockEntity.synth instanceof OscillatorSynth synth))
            return InteractionResult.PASS;

        Optional<Vec2> hitPos = getHitPos(hitResult, orientation);
        if (hitPos.isEmpty()) return InteractionResult.PASS;

        Vec2 screenPos = new Vec2(
                (hitPos.get().x - 2F / 16F) * 12F / 9F,
                (hitPos.get().y - 7F / 16F) * 16F / 7F
        );
        if (screenPos.x < 0 || screenPos.x > 1 || screenPos.y < -0.15F || screenPos.y > 1.15F)
            return InteractionResult.PASS;

        if (!level.isClientSide()) synth.setHarmonic(
                Mth.floor((1F - screenPos.x) * synth.getHarmonics().length),
                screenPos.y
        );
        return InteractionResult.SUCCESS;
    }
}
