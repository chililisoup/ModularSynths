package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;

public class LfoSynth extends AbstractSynth {
    private double phase = 0.0;

    public LfoSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public int[] dependenciesFor(int outPort) {
        return new int[]{0};
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        double[] samples = inputs.get(size).monoSamples(size);

        for (int i = 0; i < size; i++) {
            double frequency = samples[i] * 5.0;
            samples[i] = SynthesisFunctions.sineWave(this.phase + 1.0) / 2.0;
            this.phase += SynthesisFunctions.waveStep(frequency);
        }

        this.phase = this.phase % 1.0;
        return new PolySampleSource(samples);
    }

    @Override
    public void powerOff() {
        this.phase = 0.0;
    }
}
