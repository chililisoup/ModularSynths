package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.NoteShifterSynth;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

public class NoteShifterBlock extends AbstractNoteHolderBlock<NoteShifterSynth> {
    private static final MapCodec<NoteShifterBlock> CODEC = simpleCodec(NoteShifterBlock::new);
    private static final Vec2[] INPUT_POSITION = new Vec2[]{new Vec2(13F / 16F, 4F / 16F)};
    private static final Vec2[] OUTPUT_POSITION = new Vec2[]{new Vec2(3F / 16F, 4F / 16F)};

    @Override
    protected @NonNull MapCodec<NoteShifterBlock> codec() {
        return CODEC;
    }

    public NoteShifterBlock(Properties properties) {
        super(properties, NoteShifterSynth.class);
        this.registerDefaultState(this.defaultBlockState().setValue(NOTE, 12));
    }

    @Override
    public @NonNull NoteShifterSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new NoteShifterSynth(synthBlockEntity);
    }

    @Override
    public Vec2[] inputPositions() {
        return INPUT_POSITION;
    }

    @Override
    public Vec2[] outputPositions() {
        return OUTPUT_POSITION;
    }
}
