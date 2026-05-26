package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.*;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class EnvelopeSynth extends AbstractSynth {
    private final Int2DoubleOpenHashMap envelopePositions = new Int2DoubleOpenHashMap();

    public EnvelopeSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public int[] dependenciesFor(int outPort) {
        return new int[]{0, 1, 2, 3, 4, 5};
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        double[][] polySamples = inputs.get(size).safePolySamples();
        PolySampleSource controlSource = inputs.get(1, size);
        if (controlSource.channels() == 0) return new PolySampleSource(polySamples);

        PolySampleSource attackSource = inputs.get(2, size);
        PolySampleSource decaySource = inputs.get(3, size);
        PolySampleSource sustainSource = inputs.get(4, size);
        PolySampleSource releaseSource = inputs.get(5, size);

        for (int channel = 0; channel < polySamples.length; channel++) {
            double envelopePosition = this.envelopePositions.getOrDefault(channel, 0.0);
            double[] samples = polySamples[channel];

            double[] controlSamples = controlSource.unsafeChannelSamples(channel, size);
            double[] attackSamples = attackSource.unsafeChannelSamples(channel, size);
            double[] decaySamples = decaySource.unsafeChannelSamples(channel, size);
            double[] sustainSamples = sustainSource.unsafeChannelSamples(channel, size);
            double[] releaseSamples = releaseSource.unsafeChannelSamples(channel, size);

            for (int i = 0; i < size; i++) {
                double control = Math.abs(controlSamples[i]);
                double attack = Math.abs(attackSamples[i]) * ModularSynths.SAMPLE_RATE;
                double decay = Math.abs(decaySamples[i]) * ModularSynths.SAMPLE_RATE;
                double sustain = Math.abs(sustainSamples[i]);
                double release = Math.abs(releaseSamples[i]) * ModularSynths.SAMPLE_RATE;

                if (control > 0.5 && envelopePosition < 0) envelopePosition = 0;
                if (control > 0.5 || envelopePosition < 0) envelopePosition++;
                if (control < 0.5 && envelopePosition > 0) envelopePosition = (int) Math.round(-release);

                if (envelopePosition == 0) {
                    samples[i] = 0;
                } else if (envelopePosition < 0) {
                    samples[i] *= (-envelopePosition * sustain) / release;
                } else if (envelopePosition < attack) {
                    samples[i] *= 1.0 - ((attack - envelopePosition) / attack);
                } else if (envelopePosition < attack + decay) {
                    samples[i] *= ((decay - envelopePosition + attack) / decay) * (1.0 - sustain) + sustain;
                } else samples[i] *= sustain;
            }

            this.envelopePositions.put(channel, envelopePosition);
        }

        return new PolySampleSource(polySamples);
    }

    @Override
    public void powerOff() {
        this.envelopePositions.clear();
    }
}
