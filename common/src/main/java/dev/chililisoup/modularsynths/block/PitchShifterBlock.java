package dev.chililisoup.modularsynths.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.renderer.SynthBlockRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
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
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class PitchShifterBlock extends SynthBlock {
    public static final IntegerProperty NOTE;

    public PitchShifterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(NOTE, 0));
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(13F / 16F, 4F / 16F)
        };
    }

    @Override
    public Vec2[] getOutputPositions() {
        return new Vec2[]{
                new Vec2(3F / 16F, 4F / 16F)
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestOutputData(double[][] inputStackSet, int size, int outputPort, BlockState state, SynthBlockEntity blockEntity) {
        double shift = (state.getValue(NOTE) - 12.0) / 64.0;
        double[] output = inputStackSet[0];
        for (int i = 0; i < size; i++) output[i] += shift;
        return output;
    }

    @Override
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
    ) {
        BlockState blockState = blockEntity.getBlockState();
        FrontAndTop fat = blockState.getValue(BlockStateProperties.ORIENTATION);

        poseStack.pushPose();
        SynthBlockRenderer.transformPoseStackToFront(poseStack, fat);
        poseStack.scale(-0.02F, -0.02F, -0.02F);

        int note = blockState.getValue(NOTE);
        String text = String.format("%s%d", note > 12 ? "+" : "", note - 12);

        Font font = context.getFont();
        font.drawInBatch(
                text,
                (float)(-font.width(text) / 2),
                -12.0F,
                16777215,
                true,
                poseStack.last().pose(),
                buffer,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                LevelRenderer.getLightColor(level, blockEntity.getBlockPos().relative(fat.front()))
        );

        poseStack.popPose();
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        FrontAndTop fat = state.getValue(BlockStateProperties.ORIENTATION);
        if (!hit.getDirection().equals(fat.front())) return InteractionResult.PASS;

        Optional<Vec2> hitPos = SynthBlock.getHitPosition(hit, fat);
        if (hitPos.isEmpty()) return InteractionResult.PASS;

        int hitPort = SynthBlock.hitPort(hitPos.get(), this.getOutputPositions());
        if (hitPort >= 0) return InteractionResult.PASS;

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        state = state.cycle(NOTE);
        level.setBlock(pos, state, 3);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void addBlockStates(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NOTE);
    }

    static {
        NOTE = BlockStateProperties.NOTE;
    }
}
