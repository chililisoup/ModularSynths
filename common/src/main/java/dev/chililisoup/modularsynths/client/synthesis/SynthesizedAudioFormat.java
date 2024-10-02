package dev.chililisoup.modularsynths.client.synthesis;

import javax.sound.sampled.AudioFormat;

public class SynthesizedAudioFormat extends AudioFormat {
    public SynthesizedAudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
        super(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
