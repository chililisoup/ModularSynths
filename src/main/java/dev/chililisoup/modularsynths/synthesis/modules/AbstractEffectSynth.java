package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;

public abstract class AbstractEffectSynth extends AbstractSynth {
    public AbstractEffectSynth(SynthBlockEntity synthBlockEntity) {
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
        double[][] polySamples = inputs.get(size).safePolySamples();
        PolySampleSource controlSource = inputs.get(1, size);
        if (controlSource.channels() == 0) return new PolySampleSource(polySamples);

        for (int channel = 0; channel < polySamples.length; channel++) this.applyEffect(
                polySamples[channel],
                controlSource.unsafeChannelSamples(channel, size)
        );

        return new PolySampleSource(polySamples);
    }

    protected abstract void applyEffect(double[] samples, double[] control);
}
