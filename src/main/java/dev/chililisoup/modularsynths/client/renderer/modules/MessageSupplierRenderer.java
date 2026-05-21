package dev.chililisoup.modularsynths.client.renderer.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.client.renderer.AbstractSynthModuleRenderer;
import dev.chililisoup.modularsynths.client.renderer.SynthModuleRenderState;
import dev.chililisoup.modularsynths.synthesis.MessageSupplierSynth;
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
public class MessageSupplierRenderer extends AbstractSynthModuleRenderer<MessageSupplierSynth, MessageSupplierRenderer.MessageRenderState> {
    public MessageSupplierRenderer() {
        super(MessageSupplierSynth.class);
    }

    @Override
    protected void submit(
            MessageRenderState state,
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
                -font.width(state.message) / 2F,
                -12,
                state.message,
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
    protected void extractRenderState(MessageSupplierSynth synth, MessageRenderState state, float partialTicks, FrontAndTop orientation) {
        super.extractRenderState(synth, state, partialTicks, orientation);
        state.message = FormattedCharSequence.forward(synth.getMessage(), Style.EMPTY);
    }

    @Override
    protected MessageRenderState createRenderState() {
        return new MessageRenderState();
    }

    public static class MessageRenderState extends SynthModuleRenderState {
        public FormattedCharSequence message;
    }
}
