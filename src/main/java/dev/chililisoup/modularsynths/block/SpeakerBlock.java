package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.ClientSynthSpeaker;
import dev.chililisoup.modularsynths.synthesis.SynthSpeaker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SpeakerBlock extends SynthBlock<SynthSpeaker> {
    private static final MapCodec<SpeakerBlock> CODEC = simpleCodec(SpeakerBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final Vec2[] INPUT_POSITION = new Vec2[]{
            new Vec2(8F / 16F, 12F / 16F)
    };

    @Override
    protected @NonNull MapCodec<SpeakerBlock> codec() {
        return CODEC;
    }

    public SpeakerBlock(Properties properties) {
        super(properties, SynthSpeaker.class);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public @NonNull SynthSpeaker newSynth(SynthBlockEntity synthBlockEntity) {
        return ModularSynths.isClientSide() ?
                new ClientSynthSpeaker(synthBlockEntity) :
                new SynthSpeaker(synthBlockEntity);
    }

    @Override
    public Vec2[] inputPositions() {
        return INPUT_POSITION;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null ?
                state.setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())) :
                null;
    }

    @Override
    protected void neighborChanged(
            @NonNull BlockState state,
            Level level,
            @NonNull BlockPos pos,
            @NonNull Block block,
            @Nullable Orientation orientation,
            boolean movedByPiston
    ) {
        if (level.isClientSide()) return;

        boolean power = level.hasNeighborSignal(pos);
        if (power == state.getValue(POWERED)) return;

        level.setBlock(pos, state.setValue(POWERED, power), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
}
