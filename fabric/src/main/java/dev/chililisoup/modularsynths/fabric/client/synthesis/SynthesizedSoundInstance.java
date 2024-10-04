package dev.chililisoup.modularsynths.fabric.client.synthesis;

import dev.chililisoup.modularsynths.client.synthesis.AudioStreamSupplier;
import dev.chililisoup.modularsynths.client.synthesis.BaseSoundInstance;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class SynthesizedSoundInstance extends BaseSoundInstance {
    public SynthesizedSoundInstance(AudioStreamSupplier audioStreamSupplier, double x, double y, double z) {
        super(audioStreamSupplier, x, y, z);
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        this.audioStream = new SynthesizedAudioStream(this.audioStreamSupplier);
        return CompletableFuture.completedFuture(audioStream);
    }
}
