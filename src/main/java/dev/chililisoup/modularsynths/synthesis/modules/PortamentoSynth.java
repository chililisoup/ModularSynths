package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class PortamentoSynth extends AbstractSynth {
    private final Int2ObjectOpenHashMap<Status> channelStatuses = new Int2ObjectOpenHashMap<>();

    public PortamentoSynth(SynthBlockEntity synthBlockEntity) {
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
        double[][] polyControl = inputs.get(1, size).polySamples();
        if (polyControl.length == 0) return new PolySampleSource(polySamples);

        for (int channel = 0; channel < polySamples.length; channel++) {
            this.channelStatuses.put(channel, processChannel(
                    polySamples[channel],
                    channel < polyControl.length ?
                            polyControl[channel] : polyControl[polyControl.length - 1],
                    this.channelStatuses.getOrDefault(channel, Status.EMPTY)
            ));
        }

        return new PolySampleSource(polySamples);
    }

    private static Status processChannel(double[] samples, double[] control, Status status) {
        double from = status.from;
        double target = status.target;
        double last = status.last;
        boolean snapNext = status.snapNext;

        for (int i = 0; i < samples.length; i++) {
            if (snapNext) {
                from = samples[i];
                target = samples[i];
                last = samples[i];

                snapNext = control[i] == 0.0;
                continue;
            }
            snapNext = control[i] == 0.0;

            if (samples[i] == last && last == target) continue;

            if (target != samples[i]) {
                from = last;
                target = samples[i];
            }

            double step = (target - from) / (Math.max(Math.abs(control[i]), 0.01) * ModularSynths.SAMPLE_RATE);
            double oldValue = last;
            double newValue = oldValue + step;

            if ((oldValue < target && newValue >= target) || (oldValue > target && newValue <= target)) {
                newValue = target;
                from = target;
            }

            last = newValue;
            samples[i] = newValue;
        }

        return new Status(from, target, last, snapNext);
    }

    @Override
    public void powerOff() {
        this.channelStatuses.clear();
    }

    private record Status(double from, double target, double last, boolean snapNext) {
        private static final Status EMPTY = new Status(0.0, 0.0, 0.0, true);
    }
}
