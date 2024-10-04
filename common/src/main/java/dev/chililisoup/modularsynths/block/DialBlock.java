package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;
import java.util.HashMap;

public class DialBlock extends SynthBlock {
    public static final IntegerProperty NOTE;

    public DialBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(NOTE, 0));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public short[] requestData(HashMap<String, short[]> inputStack, int size, BlockState state) {
        short[] output = new short[size];
        Arrays.fill(output, (short) ((((double) state.getValue(NOTE) - 12.0) / 12.0) * 32767.0));
        return output;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        state = state.cycle(NOTE);
        level.setBlock(pos, state, 3);
        return InteractionResult.CONSUME;
    }

    @Override
    public Direction[] getOutputs(BlockState state) {
        return new Direction[]{state.getValue(FACING)};
    }

    @Override
    public boolean sendsOutput() {
        return true;
    }

    @Override
    public boolean acceptsInput() {
        return false;
    }

    @Override
    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NOTE);
    }

    static {
        NOTE = BlockStateProperties.NOTE;
    }
}
