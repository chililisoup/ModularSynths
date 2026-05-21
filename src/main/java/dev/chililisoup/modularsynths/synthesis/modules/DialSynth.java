package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.DialBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.*;

import java.util.Arrays;

public class DialSynth extends MessageSupplierSynth {
    public DialSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public String getMessage() {
        return String.format("%.1f%%", this.getValue() * 100);
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
