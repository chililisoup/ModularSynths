package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

import java.util.function.Function;

public class WaveSourceSynth extends AbstractSynth {
    private Int2DoubleOpenHashMap phases = new Int2DoubleOpenHashMap();
    private Int2DoubleOpenHashMap previousPhases = new Int2DoubleOpenHashMap();

    public WaveSourceSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public int[] dependenciesFor(int outPort) {
        return new int[]{0};
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return switch (outPort) {
            case 0 -> (inputs, size) -> this.process(inputs, size, SynthesisFunctions::sineWave, 0.25);
            case 1 -> (inputs, size) -> this.process(inputs, size, SynthesisFunctions::squareWave, 0.15);
            case 2 -> (inputs, size) -> this.process(inputs, size, SynthesisFunctions::triangleWave, 0.35);
            default -> (inputs, size) -> this.process(inputs, size, SynthesisFunctions::sawtoothWave, 0.25);
        };
    }

    private PolySampleSource process(InputSampleSource inputs, int size, Function<Double, Double> waveFunction, double amplitude) {
        PolySampleSource polyInputSamples = inputs.get(size);
        double[][] polySamples = new double[polyInputSamples.channels()][];

        for (int channel = 0; channel < polyInputSamples.channels(); channel++) {
            double[] inputSamples = polyInputSamples.polySamples()[channel];
            double[] samples = new double[size];

            double phase = this.previousPhases.get(channel);
            for (int i = 0; i < size; i++) {
                double frequency = SynthesisFunctions.getFrequencyFromDouble(inputSamples[i]);
                samples[i] = waveFunction.apply(phase) * amplitude;
                phase += SynthesisFunctions.waveStep(frequency);
            }

            this.phases.put(channel, phase);
            polySamples[channel] = samples;
        }

        return new PolySampleSource(polySamples);
    }

    @Override
    public Runnable bufferCleanupTask() {
        return this::updatePhases;
    }

    private void updatePhases() {
        this.previousPhases = this.phases;
        this.phases = new Int2DoubleOpenHashMap();
    }

    @Override
    public void powerOff() {
        this.phases.clear();
        this.previousPhases.clear();
    }
}
