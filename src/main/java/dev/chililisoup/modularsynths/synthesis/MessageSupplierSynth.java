package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.network.chat.Component;

public abstract class MessageSupplierSynth extends AbstractSynth {
    public MessageSupplierSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public abstract Component getMessage();
}
