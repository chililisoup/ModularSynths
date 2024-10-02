package dev.chililisoup.modularsynths.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
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
                .setValue(DOWN, down.is(this) || SynthBlock.class.isAssignableFrom(down.getBlock().getClass()))
                .setValue(UP, up.is(this) || SynthBlock.class.isAssignableFrom(up.getBlock().getClass()))
                .setValue(NORTH, north.is(this) || SynthBlock.class.isAssignableFrom(north.getBlock().getClass()))
                .setValue(EAST, east.is(this) || SynthBlock.class.isAssignableFrom(east.getBlock().getClass()))
                .setValue(SOUTH, south.is(this) || SynthBlock.class.isAssignableFrom(south.getBlock().getClass()))
                .setValue(WEST, west.is(this) || SynthBlock.class.isAssignableFrom(west.getBlock().getClass()));
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        boolean bl = neighborState.is(this) || SynthBlock.class.isAssignableFrom(neighborState.getBlock().getClass());
        return state.setValue(PROPERTY_BY_DIRECTION.get(direction), bl);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN);
    }
}
