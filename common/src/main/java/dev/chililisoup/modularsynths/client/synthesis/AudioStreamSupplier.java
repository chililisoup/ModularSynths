package dev.chililisoup.modularsynths.client.synthesis;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.BufferUtils;

import java.nio.ShortBuffer;

public class AudioStreamSupplier {
    private final Level level;
    private final BlockPos pos;
    private final SynthBlockEntity synthBlockEntity;

    public AudioStreamSupplier(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
        this.synthBlockEntity = (SynthBlockEntity) level.getBlockEntity(pos);
    }

    public ShortBuffer get(int size) {
        short[] soundData = this.synthBlockEntity.request(size);

        ShortBuffer shortBuffer = BufferUtils.createShortBuffer(soundData.length);
        shortBuffer.put(soundData);
        shortBuffer.position(0);

        return shortBuffer;
    }

    public interface SoundBufferSupplier {
        short[] get(BlockState state, int size);
    }
}
