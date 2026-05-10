package dev.chililisoup.modularsynths.client.audio;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.synthesis.SynthSpeaker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class SynthesizedAudioStream implements AudioStream {
    private final SynthAudioFormat audioFormat;
    private final SynthSpeaker synth;
    private final Supplier<Boolean> aliveChecker;

    public SynthesizedAudioStream(SynthSpeaker synth, Supplier<Boolean> aliveChecker) {
        this.audioFormat = new SynthAudioFormat((float) ModularSynths.SAMPLE_RATE, 16, 1, true, false);
        this.synth = synth;
        this.aliveChecker = aliveChecker;
    }

    @Override
    public @NonNull AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public @NonNull ByteBuffer read(final int expectedSize) {
        if (!this.isStreaming()) return BufferUtils.createByteBuffer(0);
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(expectedSize);

        short[] samples = this.synth.feed(expectedSize / 2);
        for (short sample : samples) {
            // https://stackoverflow.com/questions/2188660/convert-short-to-byte-in-java
            byteBuffer.put((byte)(sample & 0xff)); // little endian
            byteBuffer.put((byte)((sample >> 8) & 0xff));
        }

        return byteBuffer.clear();
    }

    @Override
    public void close() {}

    public boolean isStreaming() {
        return this.synth.isStreaming() && aliveChecker.get();
    }

    public static class SynthAudioFormat extends AudioFormat {
        public SynthAudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
            super(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        }
    }
}
