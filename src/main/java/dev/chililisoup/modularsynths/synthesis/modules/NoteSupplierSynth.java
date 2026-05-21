package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.DialBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.*;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;

import java.util.Arrays;

public class NoteSupplierSynth extends MessageSupplierSynth {
    public NoteSupplierSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public String getMessage() {
        return SynthesisFunctions.getNoteName(this.getNote());
    }

    public int getNote() {
        return this.synthBlockEntity.getBlockState().getValueOrElse(DialBlock.NOTE, 0);
    }

    private double getPitch() {
        // +57 makes it start at F# to match vanilla note blocks
        return SynthesisFunctions.getDoubleFromNote(this.getNote() + 57);
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        double[] samples = new double[size];
        Arrays.fill(samples, this.getPitch());
        return new PolySampleSource(samples);
    }
}
