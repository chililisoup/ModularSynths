package dev.chililisoup.modularsynths.mixin.client;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.gui.MidiInputScreen;
import dev.chililisoup.modularsynths.client.gui.SamplerScreen;
import dev.chililisoup.modularsynths.inject.ModularSynthsPlayer;
import dev.chililisoup.modularsynths.synthesis.modules.MidiInputSynth;
import dev.chililisoup.modularsynths.synthesis.modules.SamplerSynth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements ModularSynthsPlayer {
    @Shadow @Final protected Minecraft minecraft;

    @Override
    public void modularSynths$openSynthScreen(SynthBlockEntity synthBlockEntity) {
        if (synthBlockEntity.synth instanceof MidiInputSynth synth)
            this.minecraft.setScreen(new MidiInputScreen(synth));
        else if (synthBlockEntity.synth instanceof SamplerSynth synth)
            this.minecraft.setScreen(new SamplerScreen(synth));
    }
}
