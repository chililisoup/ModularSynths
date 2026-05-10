package dev.chililisoup.modularsynths.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class SynthRenderer implements BlockEntityRenderer<SynthBlockEntity, SynthRenderState> {
    public static final int INPUT_COLOR = 0xff00bbff;
    public static final int OUTPUT_COLOR = 0xffff8c00;
    public static final int BASE_COLOR = 0xffcccccc;
    public static final int REMOVE_COLOR = 0xfff72e40;

    public SynthRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void submit(
            @NonNull SynthRenderState state,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            @NonNull CameraRenderState camera
    ) {
        state.cables.forEach(cable -> {
            if (cable.selectionExists() && !cable.selected())
                submitNodeCollector.modularSynths$submitTranslucentCable(poseStack, cable);
            else submitNodeCollector.modularSynths$submitCable(poseStack, cable);
        });
    }

    @Override
    public void extractRenderState(
            @NonNull SynthBlockEntity blockEntity,
            @NonNull SynthRenderState state,
            float partialTicks,
            @NonNull Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        BlockState blockState = blockEntity.getBlockState();
        state.synth = blockEntity.synth;
        state.orientation = blockState.getValueOrElse(SynthBlock.ORIENTATION, FrontAndTop.NORTH_UP);

        Level level = blockEntity.getLevel();
        if (level == null) {
            state.cables = List.of();
            return;
        }

        List<CableRenderState> cableRenderStates = CableRenderState.getCableRenderStates(state.synth);
        if (cableRenderStates.isEmpty()) {
            state.cables = List.of();
            return;
        }

        BlockPos blockPos = blockEntity.getBlockPos();
        BlockPos startPos = blockPos.relative(state.orientation.front());
        int startBlockLight = level.getBrightness(LightLayer.BLOCK, startPos);
        int startSkyLight = level.getBrightness(LightLayer.SKY, startPos);

        LocalPlayer player = Minecraft.getInstance().player;
        boolean drawing = player != null && player.hasAttached(ModularSynths.CABLE_DRAW);
        CableSelection cableSelection = !drawing && player != null ?
                CableSelection.extract(state, level) : null;

        boolean selectionExists = drawing || cableSelection != null;
        boolean selectionExistsForAll = cableSelection == null
                || cableSelection.pos.equals(blockPos);

        state.cables = cableRenderStates.stream().map(cableState -> {
            boolean thisSelected;
            boolean thisSelectionExists;
            if (selectionExistsForAll) {
                thisSelectionExists = selectionExists;

                thisSelected = cableSelection != null && ((
                                cableSelection.isInput
                                && cableSelection.port == cableState.inPort
                        ) || (
                                !cableSelection.isInput
                                && cableSelection.port == cableState.outPort
                                && cableState.endBlock.equals(blockPos)
                        ));
            } else {
                thisSelectionExists = cableSelection.pos.equals(cableState.endBlock);
                thisSelected = thisSelectionExists
                        && !cableSelection.isInput
                        && cableSelection.port == cableState.outPort;
            }

            BlockPos endPos = cableState.endBlock.relative(level.getBlockState(cableState.endBlock)
                    .getValueOrElse(SynthBlock.ORIENTATION, FrontAndTop.NORTH_UP).front()
            );
            return new CableFeatureRenderState(
                    cableState,
                    thisSelected,
                    thisSelectionExists,
                    startBlockLight,
                    level.getBrightness(LightLayer.BLOCK, endPos),
                    startSkyLight,
                    level.getBrightness(LightLayer.SKY, endPos)
            );
        }).toList();
    }

    @Override
    public @NonNull SynthRenderState createRenderState() {
        return new SynthRenderState();
    }

    private record CableSelection(BlockPos pos, int port, boolean isInput) {
        private static @Nullable CableSelection extract(SynthRenderState state, Level level) {
            if (!((Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult)))
                return null;

            SynthBlockEntity blockEntity = state.synth.synthBlockEntity;
            BlockPos blockPos = hitResult.getBlockPos();
            if (blockPos.equals(state.synth.synthBlockEntity.getBlockPos())) {
                if (!(blockEntity.getBlockState().getBlock() instanceof SynthBlock<?> synthBlock))
                    return null;

                Optional<Vec2> hitPos = SynthBlock.getHitPos(hitResult, state.orientation);
                if (hitPos.isEmpty()) return null;

                int inPort = SynthBlock.hitPort(hitPos.get(), synthBlock.inputPositions());
                if (inPort >= 0) return !state.synth.inputEmpty(inPort) ?
                        new CableSelection(blockPos, inPort, true) :
                        null;

                int outPort = SynthBlock.hitPort(hitPos.get(), synthBlock.outputPositions());
                return outPort >= 0 && !state.synth.outputEmpty(outPort) ?
                        new CableSelection(blockPos, outPort, false) :
                        null;
            }

            if (!(level.getBlockEntity(blockPos) instanceof SynthBlockEntity otherBlockEntity))
                return null;

            if (!(otherBlockEntity.getBlockState().getBlock() instanceof SynthBlock<?> synthBlock))
                return null;

            Optional<Vec2> hitPos = SynthBlock.getHitPos(
                    hitResult,
                    otherBlockEntity.getBlockState().getValueOrElse(SynthBlock.ORIENTATION, FrontAndTop.NORTH_UP)
            );
            if (hitPos.isEmpty()) return null;

            AbstractSynth otherSynth = otherBlockEntity.synth;

            int inPort = SynthBlock.hitPort(hitPos.get(), synthBlock.inputPositions());
            if (inPort >= 0) return !otherSynth.inputEmpty(inPort) ?
                    new CableSelection(blockPos, inPort, true) :
                    null;

            int outPort = SynthBlock.hitPort(hitPos.get(), synthBlock.outputPositions());
            return outPort >= 0 && !otherSynth.outputEmpty(outPort) ?
                    new CableSelection(blockPos, outPort, false) :
                    null;
        }
    }
}
