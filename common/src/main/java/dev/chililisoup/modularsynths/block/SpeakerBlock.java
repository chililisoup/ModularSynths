package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.client.WaveFunctions;
import dev.chililisoup.modularsynths.client.synthesis.AudioStreamSupplier;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;

public class SpeakerBlock extends SynthBlock {
    public static final BooleanProperty POWERED;

    private int samplePosition;
    private boolean isSampling = false;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
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
    public short[] requestData(short[] data, BlockState state) {
        if (!this.isSampling) return new short[0];

        for (int i = 0; i < data.length; i++) {
            data[i] = WaveFunctions.squareWave(i + this.samplePosition, 10, 440, 0.33);
        }
        this.samplePosition += data.length;

        return data;
    }

    @Environment(EnvType.CLIENT)
    public void beginAudioStream(Level level, BlockPos pos) {
        SynthesizedAudioPlayer.playSound(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                new AudioStreamSupplier(level, pos)
        );
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (level.isClientSide && param == 1) {
            this.samplePosition = 0;
            this.beginAudioStream(level, pos);
        }
        this.isSampling = param == 1;

        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    static {
        POWERED = BlockStateProperties.POWERED;
    }
}
