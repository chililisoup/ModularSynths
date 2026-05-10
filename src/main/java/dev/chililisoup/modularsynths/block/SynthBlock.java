package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public abstract class SynthBlock<T extends AbstractSynth> extends BaseEntityBlock {
    public static final float PORT_RADIUS = 0.0625F;
    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    private final Class<T> synthClass;

    @Override
    protected abstract @NonNull MapCodec<? extends SynthBlock<T>> codec();

    public SynthBlock(Properties properties, Class<T> synthClass) {
        super(properties);
        this.synthClass = synthClass;
    }

    public abstract @NonNull T newSynth(SynthBlockEntity synthBlockEntity);

    // (0, 0) is bottom right corner
    public Vec2[] inputPositions() {
        return new Vec2[0];
    }

    public Vec2[] outputPositions() {
        return new Vec2[0];
    }

    @Override
    public final SynthBlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new SynthBlockEntity(pos, state);
    }

    public final @Nullable SynthBlockEntity getBlockEntity(LevelAccessor level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SynthBlockEntity synthBlockEntity ?
                synthBlockEntity : null;
    }

    public final @Nullable T getSynth(LevelAccessor level, BlockPos pos) {
        SynthBlockEntity synthBlockEntity = this.getBlockEntity(level, pos);
        return synthBlockEntity != null && this.synthClass.isInstance(synthBlockEntity.synth) ?
                this.synthClass.cast(synthBlockEntity.synth) : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection().getOpposite();

        Direction verticalDirection = switch (nearestLookingDirection) {
            case DOWN -> context.getHorizontalDirection().getOpposite();
            case UP -> context.getHorizontalDirection();
            case NORTH, SOUTH, WEST, EAST -> Direction.UP;
        };
        return this.defaultBlockState()
                .setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(nearestLookingDirection, verticalDirection));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    public boolean attack(
            BlockState state, Player player, Level level, InteractionHand hand, BlockPos pos, Direction face
    ) {
        if (!player.mayBuild()) return false;

        FrontAndTop orientation = state.getValueOrElse(ORIENTATION, FrontAndTop.NORTH_UP);
        if (!orientation.front().equals(face)) return false;

        AbstractSynth synth = this.getSynth(level, pos);
        if (synth == null) return false;

        BlockHitResult hitResult = ModUtil.getHitResult(level, player);
        if (!hitResult.getBlockPos().equals(pos)) return false;

        Optional<Vec2> hitPos = getHitPos(hitResult, orientation);
        if (hitPos.isEmpty()) return false;

        int inPort = SynthBlock.hitPort(hitPos.get(), this.inputPositions());
        if (inPort >= 0) {
            if (!level.isClientSide()) synth.popInputs(inPort, face);
            return true;
        }

        int outPort = SynthBlock.hitPort(hitPos.get(), this.outputPositions());
        if (outPort >= 0) {
            if (!level.isClientSide()) synth.popOutputs(outPort, face);
            return true;
        }

        return false;
    }

    public static int hitPort(Vec2 hitPos, Vec2[] ports) {
        for (int i = 0; i < ports.length; i++) {
            if (Math.abs(hitPos.x - ports[i].x) > PORT_RADIUS) continue;
            if (Math.abs(hitPos.y - ports[i].y) > PORT_RADIUS) continue;

            return i;
        }

        return -1;
    }

    public static Optional<Vec2> getHitPos(BlockHitResult hitResult, FrontAndTop orientation) {
        Direction direction = hitResult.getDirection();
        if (orientation.front() != hitResult.getDirection()) return Optional.empty();

        BlockPos blockPos = hitResult.getBlockPos().relative(direction);
        Vec3 vec3 = hitResult.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        return Optional.of(face2DPos(vec3, orientation));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static Vec2 face2DPos(Vec3 face3DPos, FrontAndTop orientation) {
        double x = face3DPos.x();
        double y = face3DPos.y();
        double z = face3DPos.z();

        Vec2 pos = switch (orientation.front()) {
            case NORTH -> new Vec2((float) x, (float) y);
            case EAST -> new Vec2((float) z, (float) y);
            case SOUTH -> new Vec2((float) (1.0 - x), (float) y);
            case WEST -> new Vec2((float) (1.0 - z), (float) y);
            case UP -> new Vec2((float) x, (float) z);
            case DOWN -> new Vec2((float) (1.0 - x), (float) (1.0 - z));
        };

        if (orientation.front().getAxis() != Direction.Axis.Y)
            return pos;

        pos = switch (orientation.top()) {
            case WEST -> new Vec2(pos.y, 1F - pos.x);
            case NORTH ->  new Vec2(1F - pos.x, 1F - pos.y);
            case EAST -> new Vec2(1F - pos.y, pos.x);
            default -> pos;
        };

        if (orientation.front() == Direction.DOWN)
            return new Vec2(pos.x, 1F - pos.y);

        return pos;
    }

    public static Vec3 face3DPos(Vec2 face2dPosition, FrontAndTop orientation) {
        double x = face2dPosition.x;
        double y = face2dPosition.y;

        Vec3 pos = switch (orientation.front()) {
            case NORTH ->   new Vec3(x,       y,   0.0    );
            case EAST ->    new Vec3(1.0,     y,   x      );
            case SOUTH ->   new Vec3(1.0 - x, y,   1.0    );
            case WEST ->    new Vec3(0.0,     y,   1.0 - x);
            case UP ->      new Vec3(x,       1.0, y      );
            case DOWN ->    new Vec3(1.0 - x, 0.0, 1.0 - y);
        };

        if (orientation.front() == Direction.UP)
            return switch (orientation.top()) {
                case WEST -> new Vec3(1F - pos.z, pos.y, pos.x);
                case NORTH ->  new Vec3(1F - pos.x, pos.y, 1F - pos.z);
                case EAST -> new Vec3(pos.z, pos.y, 1F - pos.x);
                default -> pos;
            };

        if (orientation.front() == Direction.DOWN)
            return switch (orientation.top()) {
                case WEST -> new Vec3(pos.z, pos.y, pos.x);
                case NORTH ->  new Vec3(1F - pos.x, pos.y, pos.z);
                case EAST -> new Vec3(1F - pos.z, pos.y, 1F - pos.x);
                default -> new Vec3(pos.x, pos.y, 1F - pos.z);
            };

        return pos;
    }
}
