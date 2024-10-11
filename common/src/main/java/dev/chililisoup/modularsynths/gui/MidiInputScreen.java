package dev.chililisoup.modularsynths.gui;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.MonoMidiBlock;
import dev.chililisoup.modularsynths.client.MidiInput;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class MidiInputScreen extends Screen {
    private static final Component TITLE = Component.literal("MIDI Input");
    private static final ResourceLocation image = new ResourceLocation(ModularSynths.MOD_ID, "textures/gui/midi.png");

    private final Level level;
    private final BlockPos pos;
    private final MidiInput midiInput;

    public MidiInputScreen(Level level, BlockPos pos) {
        super(TITLE);
        this.level = level;
        this.pos = pos;
        this.midiInput = new MidiInput();
    }

    public ArrayList<MidiInput.MidiNote> getNoteStack() {
        return this.midiInput.noteStack;
    }

    public int getPolyCount() {
        return this.midiInput.polyCount;
    }

    public double getPitchBend() {
        return this.midiInput.pitchBend;
    }

    public long getUpdateTime() {
        return this.midiInput.getUpdateTime();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new ImageWidget(192, 192, image));
    }

    @Override
    public void onClose() {
        this.midiInput.close();
        super.onClose();
    }

    @Override
    public void tick() {
        if (!(this.level.getBlockState(this.pos).getBlock() instanceof MonoMidiBlock)) this.onClose();
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
