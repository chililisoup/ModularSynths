package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import org.jspecify.annotations.Nullable;

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
    }
}
