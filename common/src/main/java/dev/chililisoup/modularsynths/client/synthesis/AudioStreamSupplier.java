package dev.chililisoup.modularsynths.client.synthesis;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.lwjgl.BufferUtils;

import java.nio.ShortBuffer;

public class AudioStreamSupplier {
    private final Level level;
    private final SynthBlockEntity synthBlockEntity;

    public AudioStreamSupplier(Level level, BlockPos pos) {
        this.level = level;
        this.synthBlockEntity = (SynthBlockEntity) level.getBlockEntity(pos);
    }

    public ShortBuffer get(int size) {
        short[] soundData = this.synthBlockEntity.request(level, size);

        ShortBuffer shortBuffer = BufferUtils.createShortBuffer(soundData.length);
        shortBuffer.put(soundData);
        shortBuffer.position(0);

        return shortBuffer;
    }
}
