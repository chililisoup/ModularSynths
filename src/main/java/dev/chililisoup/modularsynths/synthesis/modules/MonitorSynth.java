package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;

public class MonitorSynth extends AbstractSynth {
    private double[] storedSamples = new double[ModularSynths.SAMPLE_BUFFER_SIZE];
    private double phase = 0.0;
    private double period = 0.0;

    public MonitorSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public double[] storedSamples() {
        return this.storedSamples;
    }

    public double phase() {
        return this.phase;
    }

    public double period() {
        return this.period;
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
        double[] samples = inputs.get(1, size).monoSamples(size);
        double[][] polySamples = inputs.get(size).polySamples();
        double control = polySamples.length > 0 ? polySamples[0][0] : 0;

        double frequency = SynthesisFunctions.getFrequencyFromDouble(control);
        double period = SynthesisFunctions.wavePeriod(frequency);
        double bufferPeriodCount = size / period;
        double bufferPeriodOffset = period * (1.0 - (bufferPeriodCount % 1.0));

        this.storedSamples = samples;
        this.phase = (this.phase + bufferPeriodOffset) % period;
        this.period = period;

        return new PolySampleSource(samples);
    }
}
