package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.CableExplorer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CableBlock extends PipeBlock {
    public CableBlock(Properties properties) {
        super(0.125F, properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(BlockGetter level, BlockPos pos) {
        BlockState down = level.getBlockState(pos.below());
        BlockState up = level.getBlockState(pos.above());
        BlockState north = level.getBlockState(pos.north());
        BlockState east = level.getBlockState(pos.east());
        BlockState south = level.getBlockState(pos.south());
        BlockState west = level.getBlockState(pos.west());

        return this.defaultBlockState()
                .setValue(DOWN, down.is(this) || CableExplorer.canConnect(pos, pos.below(), down))
                .setValue(UP, up.is(this) || CableExplorer.canConnect(pos, pos.above(), up))
                .setValue(NORTH, north.is(this) || CableExplorer.canConnect(pos, pos.north(), north))
                .setValue(EAST, east.is(this) || CableExplorer.canConnect(pos, pos.east(), east))
                .setValue(SOUTH, south.is(this) || CableExplorer.canConnect(pos, pos.south(), south))
                .setValue(WEST, west.is(this) || CableExplorer.canConnect(pos, pos.west(), west));
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        boolean bl = neighborState.is(this) || CableExplorer.canConnect(pos, neighborPos, neighborState);
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), bl);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) CableExplorer.updateSynths(level, pos);
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !(newState.getBlock() instanceof CableBlock)) CableExplorer.updateSynths(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN);
    }
}
