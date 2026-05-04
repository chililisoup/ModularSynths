package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
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
import net.minecraft.world.phys.Vec2;

public class SynthControllerBlock extends SynthBlock {
    public static final BooleanProperty POWERED;

    public SynthControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(8F / 16F, 12F / 16F)
        };
    }

    @Override
    public Vec2[] getOutputPositions() {
        return new Vec2[]{
                new Vec2(4F / 16F, 4F / 16F),
                new Vec2(12F / 16F, 4F  / 16F)
        };
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        boolean hasPower = level.hasNeighborSignal(pos);
        if (hasPower != state.getValue(POWERED)) {
            if (hasPower) {
                this.startTone(level, pos);
            } else this.endTone(level, pos);

            level.setBlock(pos, state.setValue(POWERED, hasPower), 3);
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (!level.isClientSide) return true;

        if (level.getBlockEntity(pos) instanceof SynthBlockEntity blockEntity) {
            if (param == 1) blockEntity.beginAudioStream();
            else blockEntity.endAudioStream();
        }

        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.isClientSide && level.getBlockEntity(pos) instanceof SynthBlockEntity blockEntity)
            blockEntity.endAudioStream();

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    static {
        POWERED = BlockStateProperties.POWERED;
    }
}
