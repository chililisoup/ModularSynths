package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;

public class AmpSynth extends AbstractEffectSynth {
    public AmpSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    protected void applyEffect(double[] samples, double[] control) {
        for (int i = 0; i < samples.length; i++) samples[i] *= control[i];
    }
}
