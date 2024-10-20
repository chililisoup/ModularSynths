package dev.chililisoup.modularsynths.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;

public class OscilloscopeBlock extends SynthBlock {
    private static final int SAMPLES;

    public OscilloscopeBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] output = super.requestData(inputStack, outputDirection, size, state, blockEntity);

        ArrayList<double[]> savedStacks = blockEntity.getSavedStacks();
        if (savedStacks.isEmpty()) savedStacks.add(output);
        else savedStacks.set(0, output);

        return output;
    }

    @Override
    public Direction[] getOutputs(BlockState state) {
        return new Direction[]{ state.getValue(FACING).getCounterClockWise() };
    }

    @Override
    public Direction[] getInputs(BlockState state) {
        return new Direction[]{ state.getValue(FACING).getClockWise() };
    }

    @Override
    public boolean sendsOutput() {
        return true;
    }

    @Override
    public boolean acceptsInput() {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(SynthBlockEntity blockEntity,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight,
                       int packedOverlay,
                       BlockEntityRendererProvider.Context context)
    {
        if (blockEntity.getSavedStacks().isEmpty()) return;
        double[] data = blockEntity.getSavedStacks().get(0);

        Direction dir = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
        Vec3i norm = dir.getNormal();

        poseStack.pushPose();
        poseStack.translate(
                (((double) norm.getX()) / 1.95) + 0.5,
                (((double) norm.getY()) / 1.95) + 0.5,
                (((double) norm.getZ()) / 1.95) + 0.5
        );
        poseStack.mulPose(dir.getOpposite().getRotation());
        poseStack.mulPose(Direction.NORTH.getRotation());
        poseStack.scale(0.75F, 0.75F, 0.75F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.debugLineStrip(1));
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < SAMPLES; i += ModularSynths.GRAPHICS_RENDER_SCALE) {
            vertexConsumer.vertex(
                    matrix,
                    (float) i / SAMPLES - 0.5F,
                    (float) Mth.clamp((data[i] + 1) / 2, 0, 1) - 0.5F,
                    0
            ).color(0F, 1F, 0F, 1F).endVertex();
        }

        poseStack.popPose();
    }

    static {
        int samples = Math.min((int) ModularSynths.SAMPLE_RATE / 150, ModularSynths.SAMPLE_BUFFER_SIZE);
        SAMPLES = samples - (samples % ModularSynths.GRAPHICS_RENDER_SCALE);
    }
}
