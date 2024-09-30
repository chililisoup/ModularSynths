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
    private final AudioFormat audioFormat;
    private final ShortBuffer dataBuffer;

    public SynthesizedAudioStream(short[] soundData) {
        this.audioFormat = new AudioFormat(ModularSynths.SAMPLE_RATE, 16, 1, true, false);
        this.dataBuffer = BufferUtils.createShortBuffer(soundData.length);
        this.dataBuffer.put(soundData);
        this.dataBuffer.position(0);
    }

    @Override
    public @NotNull AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public @NotNull ByteBuffer read(int size) {
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);

        while (byteBuffer.hasRemaining() && this.dataBuffer.hasRemaining()) {
            short value = this.dataBuffer.get();
            // https://stackoverflow.com/questions/2188660/convert-short-to-byte-in-java
            byteBuffer.put((byte)(value & 0xff)); // little endian
            byteBuffer.put((byte)((value >> 8) & 0xff));
        }

        return  byteBuffer.flip();
    }

    @Override
    public void close() {
        this.dataBuffer.clear();
    }
}
