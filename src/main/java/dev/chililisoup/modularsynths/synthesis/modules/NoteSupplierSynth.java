package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.AbstractNoteHolderBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.*;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public class NoteSupplierSynth extends MessageSupplierSynth {
    public NoteSupplierSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public Component getMessage() {
        return Component.literal(SynthesisFunctions.getNoteName(this.getNote()));
    }

    public int getNote() {
        return this.synthBlockEntity.getBlockState().getValueOrElse(AbstractNoteHolderBlock.NOTE, 0);
    }

    private double getPitch() {
        // +45 makes it start at F# to match vanilla note blocks
        return SynthesisFunctions.getDoubleFromNote(this.getNote() + 45);
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
