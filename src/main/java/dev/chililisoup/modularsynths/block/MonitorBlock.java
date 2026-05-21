package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.block.state.ModBlockStateProperties;
import dev.chililisoup.modularsynths.block.state.MonitorDisplay;
import dev.chililisoup.modularsynths.synthesis.modules.MonitorSynth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

// TODO: Make monitors work without pull-through like the old version
public class MonitorBlock extends AbstractSynthBlock<MonitorSynth> {
    private static final MapCodec<MonitorBlock> CODEC = simpleCodec(MonitorBlock::new);
    public static final EnumProperty<MonitorDisplay> DISPLAY = ModBlockStateProperties.MONITOR_DISPLAY;
    private static final Vec2[] INPUT_POSITIONS = new Vec2[]{
            new Vec2(13F / 16F, 4F / 16F),
            new Vec2(8F / 16F, 4F / 16F)
    };
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{new Vec2(3F / 16F, 4F / 16F)};

    @Override
    protected @NonNull MapCodec<MonitorBlock> codec() {
        return CODEC;
    }

    public MonitorBlock(Properties properties) {
        super(properties, MonitorSynth.class);
        this.registerDefaultState(this.defaultBlockState().setValue(DISPLAY, MonitorDisplay.STRAIGHT));
    }

    @Override
    public Vec2[] inputPositions() {
        return INPUT_POSITIONS;
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITION;
    }

    @Override
    public @NonNull MonitorSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new MonitorSynth(synthBlockEntity);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hitResult
    ) {
        FrontAndTop orientation = state.getValueOrElse(ORIENTATION, FrontAndTop.NORTH_UP);
        if (hitResult.getDirection() != orientation.front())
            return InteractionResult.PASS;

        if (getPortHit(state, orientation, hitResult).isPresent())
            return InteractionResult.PASS;

        if (!level.isClientSide())
            level.setBlock(pos, state.cycle(DISPLAY), Block.UPDATE_CLIENTS);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DISPLAY);
    }
}
