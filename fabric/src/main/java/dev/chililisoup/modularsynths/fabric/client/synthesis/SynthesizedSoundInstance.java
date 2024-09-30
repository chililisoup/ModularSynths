package dev.chililisoup.modularsynths.fabric.client.synthesis;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class SynthesizedSoundInstance extends AbstractSoundInstance {
    protected final short[] soundData;

    public SynthesizedSoundInstance(short[] soundData, double x, double y, double z) {
        super(
//                new ResourceLocation(ModularSynths.MOD_ID, "synthesized_sound"),
                SoundEvents.MUSIC_DISC_STAL,
                SoundSource.RECORDS,
                SoundInstance.createUnseededRandom()
        );

        this.soundData = soundData;
        this.x = x;
        this.y = y;
        this.z = z;

        this.pitch = 1.0F;
        this.volume = 3.0F;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        ModularSynths.LOGGER.info("Getting audio stream...");
//        return loader.getStream(id, repeatInstantly);
        return CompletableFuture.completedFuture(new SynthesizedAudioStream(soundData));
    }
}
