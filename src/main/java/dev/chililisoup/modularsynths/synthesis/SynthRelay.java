package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;

public final class SynthRelay extends AbstractSynth {
    public SynthRelay(SynthBlockEntity synthBlockEntity, int relays) {
        super(synthBlockEntity, relays, relays);
    }
}
