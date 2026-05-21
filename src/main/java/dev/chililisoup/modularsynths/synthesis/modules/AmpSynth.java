package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;

public class AmpSynth extends AbstractSynth {
    public AmpSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public int[] dependenciesFor(int outPort) {
        return new int[]{0, 1};
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        double[] samples = inputs.get(size).monoSamples(size);
        double[] control = inputs.get(1, size).monoSamples(size);
        for (int i = 0; i < size; i++) samples[i] *= control[i] * 2.0;
        return new PolySampleSource(samples);
    }
}
