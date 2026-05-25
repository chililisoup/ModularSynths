package dev.chililisoup.modularsynths.client.gui;

import dev.chililisoup.modularsynths.client.ModClientUtil;
import dev.chililisoup.modularsynths.network.ServerboundSampleSynthUpdatePayload;
import dev.chililisoup.modularsynths.synthesis.modules.SamplerSynth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@Environment(EnvType.CLIENT)
public class SamplerScreen extends Screen {
    private final Collection<Identifier> availableSounds = ModClientUtil.availableSounds();
    private final SamplerSynth synth;
    private @Nullable Identifier sampleLocation;
    private EditBox input;

    public SamplerScreen(SamplerSynth synth) {
        super(synth.synthBlockEntity.getBlockState().getBlock().getName());
        this.synth = synth;
        this.sampleLocation = this.synth.getSampleLocation();
    }

    @Override
    protected void init() {
        this.input = new EditBox(
                this.minecraft.fontFilterFishy,
                this.width / 2 - 100,
                this.height / 2 - 10,
                200,
                20,
                Component.translatable("modularsynths.gui.sample_screen_edit_box")
        );
        this.input.setMaxLength(256);
        if (this.sampleLocation != null) this.input.setValue(this.sampleLocation.toString());
        this.input.setResponder(this::onEdited);
        this.input.addFormatter((text, _) -> this.formatChat(text));
        this.input.setCanLoseFocus(false);
        this.addRenderableWidget(this.input);

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, _ -> this.onClose())
                        .bounds(this.width / 2 - 100, this.height / 2 + 24, 200, 20)
                        .build()
        );
    }

    private void onEdited(String text) {
        Identifier sampleLocation = Identifier.tryParse(text);
        this.sampleLocation = sampleLocation != null && this.availableSounds.contains(sampleLocation) ?
                sampleLocation : null;
    }

    private FormattedCharSequence formatChat(String text) {
        return FormattedCharSequence.forward(text, Style.EMPTY.withColor(
                this.sampleLocation == null ? ChatFormatting.RED : ChatFormatting.YELLOW
        ));
    }

    @Override
    public void tick() {
        if (this.isInvalid()) this.onClose();
    }

    private boolean isInvalid() {
        return this.minecraft.player == null || this.synth.synthBlockEntity.isRemoved();
    }

    @Override
    public void removed() {
        if (this.sampleLocation == null || this.isInvalid()) return;
        if (this.synth.getSampleLocation().equals(this.sampleLocation)) return;

        ClientPlayNetworking.send(new ServerboundSampleSynthUpdatePayload(
                this.synth.synthBlockEntity.getBlockPos(), this.sampleLocation
        ));
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractTransparentBackground(@NonNull GuiGraphicsExtractor graphics) {}
}
