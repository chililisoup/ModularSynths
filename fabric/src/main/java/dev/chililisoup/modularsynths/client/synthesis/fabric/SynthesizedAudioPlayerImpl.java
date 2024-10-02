package dev.chililisoup.modularsynths.client.synthesis.fabric;

import dev.chililisoup.modularsynths.client.synthesis.AudioStreamSupplier;
import dev.chililisoup.modularsynths.fabric.client.synthesis.SynthesizedSoundInstance;
import net.minecraft.client.Minecraft;

public class SynthesizedAudioPlayerImpl {
    public static void playSound(double x, double y, double z, AudioStreamSupplier audioStreamSupplier) {
        SynthesizedSoundInstance soundInstance = new SynthesizedSoundInstance(audioStreamSupplier, x, y, z);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }
}
