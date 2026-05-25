package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.block.state.ModBlockStateProperties;
import dev.chililisoup.modularsynths.synthesis.modules.PolyToMonoSynth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class PolyToMonoBlock extends AbstractSynthBlock<PolyToMonoSynth> {
    private static final MapCodec<PolyToMonoBlock> CODEC = simpleCodec(PolyToMonoBlock::new);
    public static final BooleanProperty SUM = ModBlockStateProperties.SUM;
    private static final Vec2[] INPUT_POSITION = new Vec2[]{new Vec2(13F / 16F, 4F / 16F)};
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{new Vec2(3F / 16F, 4F / 16F)};

    @Override
    protected @NonNull MapCodec<PolyToMonoBlock> codec() {
        return CODEC;
    }

    public PolyToMonoBlock(Properties properties) {
        super(properties, PolyToMonoSynth.class);
        this.registerDefaultState(this.defaultBlockState().setValue(SUM, false));
    }

    @Override
    public Vec2[] inputPositions() {
        return INPUT_POSITION;
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITION;
    }

    @Override
    public @NonNull PolyToMonoSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new PolyToMonoSynth(synthBlockEntity);
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

        if (!level.isClientSide()) level.setBlock(pos, state.cycle(SUM), Block.UPDATE_CLIENTS);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SUM);
    }
}
