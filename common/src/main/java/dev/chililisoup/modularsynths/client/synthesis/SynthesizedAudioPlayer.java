package dev.chililisoup.modularsynths.client.synthesis;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class SynthesizedAudioPlayer {
    @ExpectPlatform
    public static void playSound(double x, double y, double z, short[] soundData) {
        throw new AssertionError();
    }
}
