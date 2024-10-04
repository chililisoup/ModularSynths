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

public class PitchBlock extends SynthBlock {
    public static final IntegerProperty NOTE;

    public PitchBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public short[] requestData(short[] data, BlockState state) {
        short[] output = new short[data.length];
        Arrays.fill(output, SynthesisFunctions.getShortFromNote(state.getValue(NOTE) + 57)); // F# to match vanilla note blocks
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
    protected void setDefaultStates(BlockState state) {
        state.setValue(NOTE, 0);
    }

    @Override
    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NOTE);
    }

    static {
        NOTE = BlockStateProperties.NOTE;
    }
}
