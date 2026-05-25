package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.AbstractNoteHolderBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.*;
import net.minecraft.network.chat.Component;

public class NoteShifterSynth extends MessageSupplierSynth {
    public NoteShifterSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public Component getMessage() {
        int shift = this.getShift();
        return Component.literal(String.format("%s%d", shift > 0 ? "+" : "", shift));
    }

    public int getShift() {
        return this.synthBlockEntity.getBlockState().getValueOrElse(AbstractNoteHolderBlock.NOTE, 0) - 12;
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
        double shift = this.getShift() / 64.0;
        double[][] polySamples = inputs.get(size).safePolySamples();

        for (double[] samples : polySamples)
            for (int i = 0; i < size; i++) samples[i] += shift;

        return new PolySampleSource(polySamples);
    }
}
