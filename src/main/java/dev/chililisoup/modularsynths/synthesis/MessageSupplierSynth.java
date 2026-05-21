package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;

public abstract class MessageSupplierSynth extends AbstractSynth {
    public MessageSupplierSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public abstract String getMessage();
}
