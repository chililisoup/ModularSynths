package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.NoteSupplierSynth;
import org.jspecify.annotations.NonNull;

public class NoteSupplierBlock extends AbstractNoteHolderBlock<NoteSupplierSynth> {
    private static final MapCodec<NoteSupplierBlock> CODEC = simpleCodec(NoteSupplierBlock::new);

    @Override
    protected @NonNull MapCodec<NoteSupplierBlock> codec() {
        return CODEC;
    }

    public NoteSupplierBlock(Properties properties) {
        super(properties, NoteSupplierSynth.class);
    }

    @Override
    public @NonNull NoteSupplierSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new NoteSupplierSynth(synthBlockEntity);
    }
}
