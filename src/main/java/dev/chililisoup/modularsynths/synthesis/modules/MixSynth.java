package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;

public class MixSynth extends AbstractSynth {
    public MixSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public int[] dependenciesFor(int outPort) {
        return new int[]{0, 1, 2};
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        PolySampleSource controlSource = inputs.get(size);
        PolySampleSource sampleSourceA = inputs.get(1, size);
        if (controlSource.channels() == 0) return new PolySampleSource(sampleSourceA.safePolySamples());
        PolySampleSource sampleSourceB = inputs.get(2, size);

        int channels = Math.max(sampleSourceA.channels(), sampleSourceB.channels());
        double[][] polySamples = new double[channels][size];
        for (int channel = 0; channel < channels; channel++) {
            double[] samples = polySamples[channel];

            double[] samplesA = sampleSourceA.unsafeChannelSamples(channel, size);
            double[] samplesB = sampleSourceB.unsafeChannelSamples(channel, size);
            double[] control = controlSource.unsafeChannelSamples(channel, size);

            for (int i = 0; i < size; i++) {
                double alpha = control[i];
                samples[i] = samplesA[i] * (1.0 - alpha) + samplesB[i] * alpha;
            }
        }

        return new PolySampleSource(polySamples);
    }
}
