package dev.chililisoup.modularsynths.fabric.client.synthesis;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.client.synthesis.AudioStreamSupplier;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class SynthesizedSoundInstance extends AbstractSoundInstance {
    protected final AudioStreamSupplier audioStreamSupplier;

    public SynthesizedSoundInstance(AudioStreamSupplier audioStreamSupplier, double x, double y, double z) {
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

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        return CompletableFuture.completedFuture(new SynthesizedAudioStream(this.audioStreamSupplier));
    }
}
