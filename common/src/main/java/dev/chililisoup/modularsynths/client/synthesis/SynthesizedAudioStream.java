package dev.chililisoup.modularsynths.client.synthesis;

import dev.chililisoup.modularsynths.ModularSynths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

@Environment(EnvType.CLIENT)
public class SynthesizedAudioStream implements AudioStream {
    private final SynthesizedAudioFormat audioFormat;
    private final AudioStreamSupplier audioStreamSupplier;
    private boolean streaming = true;

    public SynthesizedAudioStream(AudioStreamSupplier audioStreamSupplier) {
        this.audioFormat = new SynthesizedAudioFormat(ModularSynths.SAMPLE_RATE, 16, 1, true, false);
        this.audioStreamSupplier = audioStreamSupplier;
    }

    @Override
    public @NotNull AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public @NotNull ByteBuffer read(int size) {
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);

        ShortBuffer shortBuffer = this.audioStreamSupplier.get(size / 2);

        while (byteBuffer.hasRemaining() && shortBuffer.hasRemaining() && streaming) {
            short value = shortBuffer.get();
            // https://stackoverflow.com/questions/2188660/convert-short-to-byte-in-java
            byteBuffer.put((byte)(value & 0xff)); // little endian
            byteBuffer.put((byte)((value >> 8) & 0xff));
        }

        return  byteBuffer.flip(); // dunno why but it *needs* to be flipped
    }

    @Override
    public void close() { }

    public boolean isStreaming() {
        return this.streaming;
    }

    public void stopStreaming() {
        this.streaming = false;
    }
}
