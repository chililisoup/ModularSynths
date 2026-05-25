package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SynthSpeaker extends AbstractSynth {
    private boolean streaming = false;
    private @Nullable SynthGraph graph = null;

    public SynthSpeaker(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public short[] feed(int size) {
        return this.graph != null ?
                this.graph.process(size) :
                new short[size];
    }

    public boolean isStreaming() {
        return this.streaming && !this.synthBlockEntity.isRemoved();
    }

    public void start() {
        if (this.synthBlockEntity.isRemoved()) return;
        this.graph = new SynthGraph(this);
        this.streaming = true;
        this.startClient();
    }

    protected void startClient() {}

    @Override
    public void stop() {
        this.graph = null;
        this.streaming = false;
        this.getConnectedSynths().forEach(AbstractSynth::powerOff);
    }

    private Set<AbstractSynth> getConnectedSynths() {
        HashMap<BlockPos, AbstractSynth> synthMap = new HashMap<>();
        addConnectedSynths(synthMap, this);
        return synthMap.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static void addConnectedSynths(HashMap<BlockPos, AbstractSynth> synthMap, AbstractSynth synth) {
        for (InPort input : synth.inputs) input.connections().forEach(connection -> {
            if (!synthMap.containsKey(connection.pos())) {
                synthMap.put(connection.pos(), connection.synth());
                if (connection.synth() instanceof AbstractSynth connectedSynth)
                    addConnectedSynths(synthMap, connectedSynth);
            }
        });
    }
}
