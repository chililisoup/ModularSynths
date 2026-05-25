package dev.chililisoup.modularsynths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public final class ModClientUtil {
    public static SoundManager soundManager() {
        return Minecraft.getInstance().getSoundManager();
    }

    public static SoundInstance getResolvedSoundInstance(Identifier sound) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(sound);
        SoundInstance soundInstance = new SimpleSoundInstance(
                soundEvent, SoundSource.RECORDS, 1, 1, SoundInstance.createUnseededRandom(), 0, 0, 0
        );
        soundInstance.resolve(soundManager());
        return soundInstance;
    }

    public static @Nullable Sound validateSoundInstance(SoundInstance soundInstance) {
        Sound sound = soundInstance.getSound();
        return sound == null || sound == SoundManager.EMPTY_SOUND ? null : sound;
    }

    public static Collection<Identifier> availableSounds() {
        return soundManager().getAvailableSounds();
    }
}
