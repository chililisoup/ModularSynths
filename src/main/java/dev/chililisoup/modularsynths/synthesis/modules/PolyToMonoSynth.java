package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.block.PolyToMonoBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.MessageSupplierSynth;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import net.minecraft.network.chat.Component;

public class PolyToMonoSynth extends MessageSupplierSynth {
    private static final Component AVG = Component.translatable("modularsynths.poly_to_mono.avg");
    private static final Component SUM = Component.translatable("modularsynths.poly_to_mono.sum");

    public PolyToMonoSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    @Override
    public Component getMessage() {
        return this.shouldSum() ? SUM : AVG;
    }

    private boolean shouldSum() {
        return this.synthBlockEntity.getBlockState().getValueOrElse(PolyToMonoBlock.SUM, false);
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
        return new PolySampleSource(inputs.get(size).monoSamples(size, this.shouldSum()));
    }
}
