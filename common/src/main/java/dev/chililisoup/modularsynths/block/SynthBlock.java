package dev.chililisoup.modularsynths.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.network.ServerboundCableRemovalPacket;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SynthBlock extends BaseEntityBlock {
    public static final EnumProperty<FrontAndTop> ORIENTATION;
    public static final float PORT_RADIUS = 0.0625F;
    public static final float CABLE_SIZE = 0.05F;

    public SynthBlock(Properties properties) {
        super(properties);
    }

    // (0, 0) is bottom right corner
    public Vec2[] getInputPositions() {
        return new Vec2[0];
    }

    public Vec2[] getOutputPositions() {
        return new Vec2[0];
    }

    @Environment(EnvType.CLIENT)
    public double[] requestOutputData(double[][] inputStackSet, int size, int outputPort, BlockState state, SynthBlockEntity blockEntity) {
        return inputStackSet[0];
    }

    @Environment(EnvType.CLIENT)
    public void render(
            Level level,
            SynthBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            BlockEntityRendererProvider.Context context
    ) {}

    public static int hitPort(Vec2 hitPos, Vec2[] ports) {
        for (int i = 0; i < ports.length; i++) {
            if (Math.abs(hitPos.x - ports[i].x) > PORT_RADIUS) continue;
            if (Math.abs(hitPos.y - ports[i].y) > PORT_RADIUS) continue;

            return i;
        }

        return -1;
    }

    public static Vec3 face3dPosition(Vec2 face2dPosition, FrontAndTop fat) {
        double x = face2dPosition.x;
        double y = face2dPosition.y;

        Vec3 pos = switch (fat.front()) {
            case NORTH ->   new Vec3(x,       y,   0.0    );
            case EAST ->    new Vec3(1.0,     y,   x      );
            case SOUTH ->   new Vec3(1.0 - x, y,   1.0    );
            case WEST ->    new Vec3(0.0,     y,   1.0 - x);
            case UP ->      new Vec3(x,       1.0, y      );
            case DOWN ->    new Vec3(1.0 - x, 0.0, 1.0 - y);
        };

        if (fat.front() == Direction.UP)
            return switch (fat.top()) {
                case WEST -> new Vec3(1F - pos.z, pos.y, pos.x);
                case NORTH ->  new Vec3(1F - pos.x, pos.y, 1F - pos.z);
                case EAST -> new Vec3(pos.z, pos.y, 1F - pos.x);
                default -> pos;
            };

        if (fat.front() == Direction.DOWN)
            return switch (fat.top()) {
                case WEST -> new Vec3(pos.z, pos.y, pos.x);
                case NORTH ->  new Vec3(1F - pos.x, pos.y, pos.z);
                case EAST -> new Vec3(1F - pos.z, pos.y, 1F - pos.x);
                default -> new Vec3(pos.x, pos.y, 1F - pos.z);
            };

        return pos;
    }

    public static Vec3 face3dPosition(Vec2 face2dPosition, FrontAndTop fat, double offset) {
        return face3dPosition(face2dPosition, fat).add(
                Vec3.atLowerCornerOf(fat.front().getNormal()).scale(offset)
        );
    }

    public static Vec2 face2dPosition(Vec3 face3dPosition, FrontAndTop fat) {
        double x = face3dPosition.x();
        double y = face3dPosition.y();
        double z = face3dPosition.z();

        Vec2 pos = switch (fat.front()) {
            case NORTH -> new Vec2((float) x, (float) y);
            case EAST -> new Vec2((float) z, (float) y);
            case SOUTH -> new Vec2((float) (1.0 - x), (float) y);
            case WEST -> new Vec2((float) (1.0 - z), (float) y);
            case UP -> new Vec2((float) x, (float) z);
            case DOWN -> new Vec2((float) (1.0 - x), (float) (1.0 - z));
        };

        if (fat.front().getAxis() != Direction.Axis.Y)
            return pos;

        pos = switch (fat.top()) {
            case WEST -> new Vec2(pos.y, 1F - pos.x);
            case NORTH ->  new Vec2(1F - pos.x, 1F - pos.y);
            case EAST -> new Vec2(1F - pos.y, pos.x);
            default -> pos;
        };

        if (fat.front() == Direction.DOWN)
            return new Vec2(pos.x, 1F - pos.y);

        return pos;
    }

    public static Optional<Vec2> getHitPosition(BlockHitResult hitResult, FrontAndTop fat) {
        Direction direction = hitResult.getDirection();
        if (fat.front() != hitResult.getDirection()) return Optional.empty();

        BlockPos blockPos = hitResult.getBlockPos().relative(direction);
        Vec3 vec3 = hitResult.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        return Optional.of(face2dPosition(vec3, fat));
    }

    public static BlockHitResult getHitResult(Level level, Player player, float partialTick) {
        Vec3 eyePos = player.getEyePosition(partialTick);

        return level.clip(new ClipContext(
                eyePos,
                eyePos.add(player.getViewVector(partialTick).scale(20)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
    }

    @Environment(EnvType.CLIENT)
    public boolean tryRemoveCables(BlockState state, Level level, BlockPos pos, Player player) {
        if (!player.getAbilities().mayBuild) return false;

        BlockHitResult hitResult = getHitResult(level, player, 1);
        if (!hitResult.getBlockPos().equals(pos)) return false;

        Optional<Vec2> hitPos = getHitPosition(hitResult, state.getValue(BlockStateProperties.ORIENTATION));
        if (hitPos.isEmpty()) return false;

        SynthBlock block = (SynthBlock) state.getBlock();
        SynthBlockEntity blockEntity = (SynthBlockEntity) level.getBlockEntity(pos);
        if (blockEntity == null) return false;

        int inputPort = SynthBlock.hitPort(hitPos.get(), block.getInputPositions());
        int outputPort = SynthBlock.hitPort(hitPos.get(), block.getOutputPositions());

        if (inputPort >= 0 || outputPort >= 0) {
            NetworkManager.sendToServer(ServerboundCableRemovalPacket.id(), ServerboundCableRemovalPacket.make(
                    pos,
                    inputPort >= 0 ? inputPort : outputPort,
                    inputPort >= 0
            ));

            return true;
        }

        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SynthBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction front = context.getNearestLookingDirection().getOpposite();
        Direction top = switch (front) {
            case DOWN -> context.getHorizontalDirection().getOpposite();
            case UP -> context.getHorizontalDirection();
            case NORTH, SOUTH, WEST, EAST -> Direction.UP;
        };

        return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(front, top));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof SynthBlockEntity synthBlockEntity) {
                synthBlockEntity.getOutputs().forEach(output -> {
                    if (!output.pos.equals(pos) && level.getBlockEntity(output.pos) instanceof SynthBlockEntity outputBlockEntity)
                        outputBlockEntity.popInputsFromOutput(pos);
                    synthBlockEntity.popOutput(output);
                });
                synthBlockEntity.getInputs().forEach(input -> {
                    if (!input.pos.equals(pos) && level.getBlockEntity(input.pos) instanceof SynthBlockEntity inputBlockEntity)
                        inputBlockEntity.popOutputsFromInput(pos);
                });
            }

            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType, ModBlockEntityTypes.SYNTH.get(), SynthBlockEntity::animationTick) : null;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ORIENTATION, rotation.rotation().rotate(state.getValue(ORIENTATION)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ORIENTATION, mirror.rotation().rotate(state.getValue(ORIENTATION)));
    }

    @Override
    protected final void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
        this.addBlockStates(builder);
    }

    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {}

    static {
        ORIENTATION = BlockStateProperties.ORIENTATION;
    }
}
