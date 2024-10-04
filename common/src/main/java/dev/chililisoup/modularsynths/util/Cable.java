package dev.chililisoup.modularsynths.util;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.CableBlock;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public abstract class Cable {
    public static ArrayList<Connection> exploreFrom(BlockPos pos, Level level) {
        Direction[] directions;
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Context context;
        ArrayList<Direction> positionDirections = new ArrayList<>();
        ArrayList<Long> positions = new ArrayList<>();
        ArrayList<Long> checkedPositions = new ArrayList<>();

        if (block instanceof CableBlock) {
            directions = filterDirections(state);
            context = Context.ANY;
        } else if (block instanceof SynthBlock) {
            directions = ((SynthBlock) block).getInputs(state);
            context = Context.INPUT;
            checkedPositions.add(pos.asLong());
        } else {
            directions = Direction.values();
            context = Context.ANY;
        }

        for (Direction direction : directions) {
            BlockPos checkPos = pos.relative(direction);
            checkPosition(pos, checkPos, level, positions, checkedPositions, 0, context);

            for (int i = positionDirections.size(); i < positions.size(); i++) {
                positionDirections.add(direction);
            }
        }

        ArrayList<Connection> connections = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            connections.add(new Connection(BlockPos.of(positions.get(i)), positionDirections.get(i)));
        }

        return connections;
    }

    private static void checkPosition(BlockPos fromPos, BlockPos checkPos, Level level, ArrayList<Long> positions, ArrayList<Long> checkedPositions, int searchDepth, Context context) {
        BlockState checkState = level.getBlockState(checkPos);
        Block checkBlock = checkState.getBlock();

        if (checkBlock instanceof CableBlock) {
            if (searchDepth < ModularSynths.MAX_SEARCH_DEPTH)
                exploreAt(checkPos, level, positions, checkedPositions, searchDepth, context);
        } else if (canConnect(fromPos, checkPos, checkState, context)) {
            checkedPositions.add(checkPos.asLong());
            positions.add(checkPos.asLong());
        }
    }

    private static void exploreAt(BlockPos pos, Level level, ArrayList<Long> positions, ArrayList<Long> checkedPositions, int searchDepth, Context context) {
        for (Direction direction : filterDirections(level.getBlockState(pos))) {
            BlockPos checkPos = pos.relative(direction);

            long longPos = checkPos.asLong();
            if (checkedPositions.contains(longPos)) continue;

            checkedPositions.add(longPos);
            checkPosition(pos, checkPos, level, positions, checkedPositions, searchDepth + 1, context);
        }
    }

    private static Direction[] filterDirections(BlockState state) {
        ArrayList<Direction> directions = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
                directions.add(direction);
            }
        }

        Direction[] directionArray = new Direction[directions.size()];
        return directions.toArray(directionArray);
    }

    public static boolean canConnect(BlockPos from, BlockPos to, BlockState toState, Context context) {
        if (!(toState.getBlock() instanceof SynthBlock synthBlock)) return false;

        if (context.checkInput) {
            for (Direction direction : synthBlock.getInputs(toState)) {
                if (to.relative(direction).equals(from)) return true;
            }
        }

        if (context.checkOutput) {
            for (Direction direction : synthBlock.getOutputs(toState)) {
                if (to.relative(direction).equals(from)) return true;
            }
        }

        return false;
    }

    public static boolean canConnect(BlockPos from, BlockPos to, BlockState toState) {
        return canConnect(from, to ,toState, Context.ANY);
    }

    public static void updateSynths(Level level, BlockPos pos) {
        Cable.exploreFrom(pos, level).forEach(connection -> {
            BlockEntity blockEntity = level.getBlockEntity(connection.position);
            if (!(blockEntity instanceof SynthBlockEntity)) return;
            ((SynthBlockEntity) blockEntity).findInputs(level);
        });
    }

    public enum Context {
        ANY(true, true),
        INPUT(false, true),
        OUTPUT(true, false);

        public final boolean checkInput;
        public final boolean checkOutput;

        Context(boolean checkInput, boolean checkOutput) {
            this.checkInput = checkInput;
            this.checkOutput = checkOutput;
        }
    }

    public static class Connection {
        public final BlockPos position;
        public final long longPos;
        public final Direction direction;
        public final int intDir;

        public Connection(BlockPos position, Direction direction) {
            this.position = position;
            this.longPos = position.asLong();
            this.direction = direction;
            this.intDir = direction.get3DDataValue();
        }

        public Connection(long longPos, int intDir) {
            this.position = BlockPos.of(longPos);
            this.longPos = longPos;
            this.direction = Direction.from3DDataValue(intDir);
            this.intDir = intDir;
        }

        public CompoundTag toTag() {
            CompoundTag connectionTag = new CompoundTag();
            connectionTag.putLong("pos", this.longPos);
            connectionTag.putInt("dir", this.intDir);
            return connectionTag;
        }
    }
}
