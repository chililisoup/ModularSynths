package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
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

import java.util.HashMap;

public abstract class SynthBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING;

    public SynthBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = new double[size];

        inputStack.forEach((direction, dataStack) -> {
            for (int i = 0; i < size; i++) outputStack[i] += dataStack[i] / inputStack.size();
        });

        return outputStack;
    }

    @Environment(EnvType.CLIENT)
    public double[][] requestPolyData(HashMap<String, double[][]> inputStackSet, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        HashMap<String, double[]> monoStack = new HashMap<>();

        inputStackSet.forEach((direction, dataSet) ->
                monoStack.put(direction, SynthesisFunctions.polyToMono(dataSet, size))
        );

        return new double[][]{this.requestData(monoStack, outputDirection, size, state, blockEntity)};
    }

    @Environment(EnvType.CLIENT)
    public int getPolyCount(HashMap<String, double[][]> inputStackSet) {
        final int[] polyCount = {1};

        inputStackSet.forEach((dir, inputStack) -> {
            if (inputStack.length > polyCount[0]) polyCount[0] = inputStack.length;
        });

        return polyCount[0];
    }

    @Environment(EnvType.CLIENT)
    public double[][] combinePolyStackSets(HashMap<String, double[][]> inputStackSet, int size) {
        double[][] combinedStack = new double[getPolyCount(inputStackSet)][size];

        inputStackSet.forEach((dir, inputStack) -> {
            for (int i = 0; i < inputStack.length; i++) {
                for (int j = 0; j < size; j++) {
                    combinedStack[i][j] += inputStack[i][j] / inputStackSet.size();
                }
            }
        });

        return combinedStack;
    }

    @Environment(EnvType.CLIENT)
    public double inputFallback() {
        return 0.0;
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

    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {}

    static {
        FACING = BlockStateProperties.FACING;
    }
}
