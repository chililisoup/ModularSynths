package dev.chililisoup.modularsynths.client.synthesis;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import net.minecraft.util.Mth;
import org.lwjgl.BufferUtils;

import java.nio.ShortBuffer;

public class AudioStreamSupplier {
    private final SynthBlockEntity synthBlockEntity;
    private boolean beginning = true;

    public AudioStreamSupplier(SynthBlockEntity synthBlockEntity) {
        this.synthBlockEntity = synthBlockEntity;
    }

    public ShortBuffer get(int size) {
        double[] soundData = SynthesisFunctions.polyToMono(this.synthBlockEntity.request(size, beginning), size);
        short[] shortData = new short[size];
        beginning = false;

        boolean clipping = false;

        for (int i = 0; i < size; i++) {
            if (Math.abs(soundData[i]) > 1.0) clipping = true;
            shortData[i] = (short) (Mth.clamp(soundData[i], -1.0, 1.0) * Short.MAX_VALUE);
        }

        if (clipping) ModularSynths.LOGGER.warn("Audio is clipping!");

        ShortBuffer shortBuffer = BufferUtils.createShortBuffer(size);
        shortBuffer.put(shortData);
        shortBuffer.position(0);

        return shortBuffer;
    }
}
