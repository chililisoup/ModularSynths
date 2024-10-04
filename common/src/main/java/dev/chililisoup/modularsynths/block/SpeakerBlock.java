package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.client.synthesis.AudioStreamSupplier;
import dev.chililisoup.modularsynths.client.synthesis.BaseSoundInstance;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.HashMap;

public class SpeakerBlock extends SynthBlock {
    public static final BooleanProperty POWERED;

    private BaseSoundInstance soundInstance;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean bl = level.hasNeighborSignal(pos);
        if (bl != state.getValue(POWERED)) {
            if (bl) {
                this.startTone(level, pos);
            } else this.endTone(level, pos);

            level.setBlock(pos, state.setValue(POWERED, bl), 3);
        }
    }

    private void startTone(Level level, BlockPos pos) {
        level.blockEvent(pos, this, 0, 1);
        level.gameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
    }

    private void endTone(Level level, BlockPos pos) {
        level.blockEvent(pos, this, 0, 0);
        level.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public short[] requestData(HashMap<String, short[]> inputStack, int size, BlockState state) {
        return super.requestData(inputStack, size, state);
    }

    @Environment(EnvType.CLIENT)
    public void beginAudioStream(Level level, BlockPos pos) {
        this.soundInstance = SynthesizedAudioPlayer.playSound(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                new AudioStreamSupplier(level, pos)
        );
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (this.soundInstance != null) this.soundInstance.stopStreaming();
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (level.isClientSide) {
            if (param == 1) this.beginAudioStream(level, pos);
            else if (this.soundInstance != null) {
                this.soundInstance.stopStreaming();
                this.soundInstance = null;
            }
        }

        return true;
    }

    @Override
    public Direction[] getInputs(BlockState state) {
        return new Direction[]{state.getValue(FACING).getOpposite()};
    }

    @Override
    public boolean sendsOutput() {
        return false;
    }

    @Override
    public boolean acceptsInput() {
        return true;
    }

    @Override
    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    static {
        POWERED = BlockStateProperties.POWERED;
    }
}
