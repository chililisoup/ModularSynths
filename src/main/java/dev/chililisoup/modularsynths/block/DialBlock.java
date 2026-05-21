package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.modules.DialSynth;
import org.jspecify.annotations.NonNull;

public class DialBlock extends AbstractNoteHolderBlock<DialSynth> {
    private static final MapCodec<DialBlock> CODEC = simpleCodec(DialBlock::new);

    @Override
    protected @NonNull MapCodec<DialBlock> codec() {
        return CODEC;
    }

    public DialBlock(Properties properties) {
        super(properties, DialSynth.class);
    }

    @Override
    public @NonNull DialSynth newSynth(SynthBlockEntity synthBlockEntity) {
        return new DialSynth(synthBlockEntity);
    }
}
