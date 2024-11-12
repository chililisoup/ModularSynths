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
import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AudioVisualizerBlock extends SynthBlock {
    private static final DoubleFFT_1D DOUBLE_FFT = new DoubleFFT_1D(ModularSynths.SAMPLE_BUFFER_SIZE);
    private static final int SIZE = 256 / ModularSynths.GRAPHICS_RENDER_SCALE;
    private static final double EXP = Math.log(ModularSynths.SAMPLE_BUFFER_SIZE) / SIZE;
    private static final int AVG_COUNT = (int) (ModularSynths.SAMPLE_RATE / (ModularSynths.SAMPLE_BUFFER_SIZE * 4));
    private static final int SMOOTHING = 16 / ModularSynths.GRAPHICS_RENDER_SCALE;

    public AudioVisualizerBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] output = super.requestData(inputStack, outputDirection, size, state, blockEntity);

        ArrayList<double[]> savedStacks = blockEntity.getSavedStacks();
        if (savedStacks.isEmpty()) savedStacks.add(output);
        else {
            savedStacks.set(0, output);
            if (savedStacks.size() > AVG_COUNT + 1) {
                savedStacks.remove(AVG_COUNT + 1);
                savedStacks.remove(1);
            }
        }

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
        ArrayList<double[]> savedStacks = blockEntity.getSavedStacks();
        double[] data;

        if (savedStacks.size() > AVG_COUNT + 1) {
            data = savedStacks.get(AVG_COUNT + 1);
        } else {
            double[] fftData = savedStacks.get(0).clone();

            for (int i = 0; i < fftData.length; i++) { // Smooths out data to reduce artifacts
                fftData[i] *= -Math.exp((double) -i / 64) - Math.exp((double) (i - fftData.length) / 64) + 1;
            }

            DOUBLE_FFT.realForward(fftData);
            savedStacks.add(fftData);

            // Make this take the FFT of the last four instead of the average of the last four FFTs
            // Or not, wouldn't that be 4x as expensive?
            if (savedStacks.size() == AVG_COUNT + 1) {
                data = new double[SIZE];

                double[] prev = new double[SMOOTHING];
                Arrays.fill(prev, -1);

                for (int i = 0; i < SIZE; i++) {
                    int index = (int) Math.round(Math.exp(EXP * i) - 1);

                    double val = 0.0;
                    for (int j = 0; j < AVG_COUNT; j++) {
                        val += savedStacks.get(j + 1)[index];
                    }
                    val = Math.log10(Math.abs(val / AVG_COUNT));

                    data[i] = Mth.clamp(
                            (Arrays.stream(prev).sum() + val) / (SMOOTHING + 1),
                            -1,
                            1
                    ) / 2;

                    for (int j = 1; j < SMOOTHING; j++) prev[j - 1] = prev[j];
                    prev[SMOOTHING - 1] = val;
                }

                savedStacks.add(data);
            } else return;
        }

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

        for (int i = 0; i < SIZE; i++) {
            vertexConsumer.vertex(
                    matrix,
                    (float) i / SIZE - 0.5F,
                    (float) data[i],
                    0
            ).color(1F, 1F, 0F, 1F).endVertex();
        }

        poseStack.popPose();
    }
}
