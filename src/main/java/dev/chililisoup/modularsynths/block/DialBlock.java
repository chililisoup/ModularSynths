package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.DialSynth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class DialBlock extends AbstractSynthBlock<DialSynth> {
    private static final MapCodec<DialBlock> CODEC = simpleCodec(DialBlock::new);
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{
            new Vec2(8F / 16F, 4F / 16F)
    };

    @Override
    protected @NonNull MapCodec<DialBlock> codec() {
        return CODEC;
    }

    public DialBlock(Properties properties) {
        super(properties, DialSynth.class);
        this.registerDefaultState(this.defaultBlockState().setValue(NOTE, 0));
    }

    @Override
    public @NonNull DialSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new DialSynth(synthBlockEntity);
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITION;
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

        if (getPortHit(state, orientation, hitResult).isPresent())
            return InteractionResult.PASS;

        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                int note = state.getValue(NOTE);
                state = state.setValue(NOTE, note > 0 ? note - 1 : NOTE.getPossibleValues().size() - 1);
            } else state = state.cycle(NOTE);

            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NOTE);
    }
}
