package dev.chililisoup.modularsynths.client.synthesis.fabric;

import dev.chililisoup.modularsynths.fabric.client.synthesis.SynthesizedSoundInstance;
import net.minecraft.client.Minecraft;

public class SynthesizedAudioPlayerImpl {
    public static void playSound(double x, double y, double z, short[] soundData) {
        SynthesizedSoundInstance soundInstance = new SynthesizedSoundInstance(soundData, x, y, z);
        Minecraft.getInstance().getSoundManager().play(soundInstance);
    }
}
