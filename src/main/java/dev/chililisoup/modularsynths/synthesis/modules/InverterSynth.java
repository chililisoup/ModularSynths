package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;

public class InverterSynth extends AbstractEffectSynth {
    public InverterSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    protected void applyEffect(double[] samples, double[] control) {
        for (int i = 0; i < samples.length; i++) samples[i] =
                Math.copySign((1.0 - Math.abs(samples[i])), samples[i])
                        * Math.abs(control[i]) +
                        (1.0 - Math.abs(control[i])) * samples[i];
    }
}
