package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.DialBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;

import java.util.Arrays;

public class DialSynth extends AbstractSynth {
    public DialSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public double getValue() {
        return this.synthBlockEntity.getBlockState().getValueOrElse(
                DialBlock.NOTE, 0
        ) / 24.0;
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        double[] samples = new double[size];
        Arrays.fill(samples, this.getValue());
        return new PolySampleSource(samples);
    }
}
