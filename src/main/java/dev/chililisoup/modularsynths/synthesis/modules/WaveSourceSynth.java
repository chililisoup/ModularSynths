package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;

import java.util.function.Function;

public class WaveSourceSynth extends AbstractSynth {
    private final WaveType sine = new WaveType(SynthesisFunctions::sineWave);
    private final WaveType square = new WaveType(SynthesisFunctions::squareWave);
    private final WaveType triangle = new WaveType(SynthesisFunctions::triangleWave);
    private final WaveType sawtooth = new WaveType(SynthesisFunctions::sawtoothWave);

    public WaveSourceSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public int[] dependenciesFor(int outPort) {
        return new int[]{outPort};
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return switch (outPort) {
            case 0 -> this.sine::process;
            case 1 -> this.square::process;
            case 2 -> this.triangle::process;
            default -> this.sawtooth::process;
        };
    }

    private static class WaveType {
        private double phase = 0.0;
        private final Function<Double, Double> waveFunction;

        WaveType(Function<Double, Double> waveFunction) {
            this.waveFunction = waveFunction;
        }

        PolySampleSource process(InputSampleSource inputs, int size) {
            double[] inputSamples = inputs.get(size).monoSamples(size);
            double[] samples = new double[size];

            for (int i = 0; i < size; i++) {
                double frequency = SynthesisFunctions.getFrequencyFromDouble(inputSamples[i]);
                samples[i] = this.waveFunction.apply(this.phase) / 4.0;
                this.phase += SynthesisFunctions.waveStep(frequency);
            }

            this.phase = this.phase % 1.0;
            return new PolySampleSource(samples);
        }
    }
}
