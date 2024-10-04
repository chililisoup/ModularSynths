package dev.chililisoup.modularsynths.client.synthesis;

import dev.chililisoup.modularsynths.ModularSynths;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class BaseSoundInstance extends AbstractSoundInstance {
    protected final AudioStreamSupplier audioStreamSupplier;
    protected SynthesizedAudioStream audioStream;

    public BaseSoundInstance(AudioStreamSupplier audioStreamSupplier, double x, double y, double z) {
        super(
                new ResourceLocation(ModularSynths.MOD_ID, "synthesized_sound"),
                SoundSource.RECORDS,
                SoundInstance.createUnseededRandom()
        );

        this.audioStreamSupplier = audioStreamSupplier;
        this.x = x;
        this.y = y;
        this.z = z;

        this.pitch = 1.0F;
        this.volume = 3.0F;
    }

    public void stopStreaming() {
        if (audioStream == null) return;
        audioStream.stopStreaming();
    }
}
