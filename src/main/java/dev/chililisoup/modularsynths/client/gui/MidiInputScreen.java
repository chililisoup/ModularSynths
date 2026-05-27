package dev.chililisoup.modularsynths.client.gui;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.synthesis.ClientMidiInput;
import dev.chililisoup.modularsynths.synthesis.modules.MidiInputSynth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class MidiInputScreen extends Screen {
    private static final int UPDATE_INTERVAL = 2;
    private static final Identifier BACKGROUND = ModularSynths.id("textures/gui/midi.png");

    private final ClientMidiInput midiInput;
    private long tickCount = 0;

    public MidiInputScreen(MidiInputSynth synth) {
        super(synth.synthBlockEntity.getBlockState().getBlock().getName());
        this.midiInput = new ClientMidiInput(synth);
    }

    @Override
    public void tick() {
        if (this.minecraft.player == null || this.midiInput.isInvalid()) {
            this.onClose();
            return;
        }

        if (this.tickCount++ % UPDATE_INTERVAL == 0)
            this.midiInput.maybeSendPayload();
    }

    @Override
    public void onClose() {
        this.midiInput.close();
        super.onClose();
    }

    @Override
    public void removed() {
        this.midiInput.close();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, 0, 0, 192, 192, 192, 192);
    }

    @Override
    public void extractTransparentBackground(@NonNull GuiGraphicsExtractor graphics) {}
}
