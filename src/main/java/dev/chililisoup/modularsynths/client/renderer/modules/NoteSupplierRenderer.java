package dev.chililisoup.modularsynths.client.renderer.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.client.renderer.AbstractSynthModuleRenderer;
import dev.chililisoup.modularsynths.client.renderer.SynthModuleRenderState;
import dev.chililisoup.modularsynths.synthesis.modules.NoteSupplierSynth;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class NoteSupplierRenderer extends AbstractSynthModuleRenderer<NoteSupplierSynth, NoteSupplierRenderer.NoteSupplierRenderState> {
    public NoteSupplierRenderer() {
        super(NoteSupplierSynth.class);
    }

    @Override
    protected void submit(
            NoteSupplierRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            BlockEntityRendererProvider.Context context
    ) {
        poseStack.pushPose();
        transformPoseStackToFront(poseStack, state.orientation);
        poseStack.scale(-0.02F, -0.02F, -0.02F);

        Font font = context.font();
        submitNodeCollector.submitText(
                poseStack,
                -font.width(state.text) / 2F,
                -12,
                state.text,
                false,
                Font.DisplayMode.POLYGON_OFFSET,
                state.frontLightCoords,
                -1,
                0,
                0
        );

        poseStack.popPose();
    }

    @Override
    protected void extractRenderState(NoteSupplierSynth synth, NoteSupplierRenderState state, float partialTicks, FrontAndTop orientation) {
        super.extractRenderState(synth, state, partialTicks, orientation);
        state.text = FormattedCharSequence.forward(SynthesisFunctions.getNoteName(synth.getNote()), Style.EMPTY);
    }

    @Override
    protected NoteSupplierRenderState createRenderState() {
        return new NoteSupplierRenderState();
    }

    public static class NoteSupplierRenderState extends SynthModuleRenderState {
        public FormattedCharSequence text;
    }
}
