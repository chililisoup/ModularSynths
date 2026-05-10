package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;

public class SinSynth extends AbstractSynth {
    private double phase = 0.0;

    public SinSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity, 0, 1);
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        double[] samples = new double[size];
        for (int i = 0; i < samples.length; i++)
            samples[i] = Math.sin((this.phase + i) * 800.0 / ModularSynths.SAMPLE_RATE);

        this.phase = (this.phase + size) % (Math.PI * ModularSynths.SAMPLE_RATE);

        return new PolySampleSource(samples);
    }
}
