package dev.chililisoup.modularsynths.client.audio;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.synthesis.SynthSpeaker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class SynthSoundInstance implements SoundInstance {
    private static final Identifier ID = ModularSynths.id("synthesized_sound");

    private final RandomSource random = SoundInstance.createUnseededRandom();
    public final BlockPos position;
    private final SynthesizedAudioStream audioStream;
    private @Nullable Sound sound = null;
    private boolean alive = true;

    public SynthSoundInstance(BlockPos position, SynthSpeaker synth) {
        this.position = position;
        this.audioStream = new SynthesizedAudioStream(synth, () -> this.alive);
    }

    public void kill() {
        this.alive = false;
        Minecraft.getInstance().getSoundManager().stop(this);
    }

    @Override
    public @NonNull CompletableFuture<AudioStream> getAudioStream(
            @NonNull SoundBufferLibrary library, @NonNull Identifier id, boolean repeatInstantly
    ) {
        return CompletableFuture.completedFuture(this.audioStream);
    }

    @Override
    public @NonNull Identifier getIdentifier() {
        return ID;
    }

    @Override
    public @Nullable WeighedSoundEvents resolve(@NonNull SoundManager soundManager) {
        WeighedSoundEvents soundEvent = soundManager.getSoundEvent(ID);
        if (soundEvent != null) this.sound = soundEvent.getSound(this.random);
        return soundEvent;
    }

    @Override
    public @Nullable Sound getSound() {
        return this.sound;
    }

    @Override
    public @NonNull SoundSource getSource() {
        return SoundSource.RECORDS;
    }

    @Override
    public boolean isLooping() {
        return true;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 1;
    }

    @Override
    public float getPitch() {
        return 1;
    }

    @Override
    public double getX() {
        return this.position.getX();
    }

    @Override
    public double getY() {
        return this.position.getY();
    }

    @Override
    public double getZ() {
        return this.position.getZ();
    }

    @Override
    public @NonNull Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }
}
