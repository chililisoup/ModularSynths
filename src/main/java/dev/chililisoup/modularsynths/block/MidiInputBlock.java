package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.block.state.ModBlockStateProperties;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import dev.chililisoup.modularsynths.synthesis.modules.MidiInputSynth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MidiInputBlock extends AbstractSynthBlock<MidiInputSynth> {
    private static final MapCodec<MidiInputBlock> CODEC = simpleCodec(MidiInputBlock::new);
    public static final BooleanProperty POLYPHONIC = ModBlockStateProperties.POLYPHONIC;
    private static final Vec2[] OUTPUT_POSITIONS = new Vec2[]{
            new Vec2(3F / 16F, 4F / 16F),
            new Vec2(8F / 16F, 4F / 16F),
            new Vec2(13F / 16F, 4F / 16F)
    };

    @Override
    protected @NonNull MapCodec<MidiInputBlock> codec() {
        return CODEC;
    }

    public MidiInputBlock(Properties properties) {
        super(properties, MidiInputSynth.class);
        this.registerDefaultState(this.defaultBlockState().setValue(POLYPHONIC, false));
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITIONS;
    }

    @Override
    public @NonNull MidiInputSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new MidiInputSynth(synthBlockEntity);
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

        if (!(synthBlockEntity.synth instanceof MidiInputSynth synth))
            return InteractionResult.PASS;

        if (this.getPortHit(orientation, hitResult).isPresent())
            return InteractionResult.PASS;

        if (!level.isClientSide()) {
            if (player.isShiftKeyDown())
                level.setBlock(pos, state.cycle(POLYPHONIC), Block.UPDATE_CLIENTS);
            else if (synth.getControllingPlayer() == null) {
                synth.setControllingPlayer(player.getUUID());
                player.modularSynths$openSynthScreen(synthBlockEntity);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            @NonNull Level level, @NonNull BlockState blockState, @NonNull BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                ModBlockEntityTypes.SYNTH,
                (_, _, _, synthBlockEntity) -> MidiInputSynth.tick(synthBlockEntity)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POLYPHONIC);
    }
}
