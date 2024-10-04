package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.CableExplorer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;

public abstract class SynthBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING;

    public SynthBlock(Properties properties) {
        super(properties);
        BlockState defaultState = this.stateDefinition.any().setValue(FACING, Direction.NORTH);
        setDefaultStates(defaultState);
        this.registerDefaultState(defaultState);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && !(oldState.getBlock() instanceof SynthBlock)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SynthBlockEntity) ((SynthBlockEntity) blockEntity).findInputs(level);
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SynthBlockEntity(pos, state);
    }

    @Environment(EnvType.CLIENT)
    public short[] requestData(short[] data, BlockState state) {
        return data;
    }

    @Environment(EnvType.CLIENT)
    public short inputFallback() {
        return 0;
    }

    public Direction[] getOutputs(BlockState state) {
        return new Direction[0];
    }

    public Direction[] getInputs(BlockState state) {
        return new Direction[0];
    }

    public abstract boolean sendsOutput();

    public abstract boolean acceptsInput();

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected final void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        this.addBlockStates(builder);
    }

    protected void setDefaultStates(BlockState state) {}

    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {}

    static {
        FACING = BlockStateProperties.FACING;
    }
}
