package dev.chililisoup.modularsynths.synthesis.modules;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.MidiInputBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class MidiInputSynth extends AbstractSynth {
    private static final int POLY_COUNT = 8;

    private final ArrayList<MidiNote> noteStack = new ArrayList<>(POLY_COUNT);
    private long time = System.currentTimeMillis();
    private double pitchBend = 0.0;

    public MidiInputSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public void addNote(byte note, byte velocity, int channel, long time) {
        this.stopNote(note, time);

        if (this.noteStack.size() == POLY_COUNT) {
            for (int i = 0; i < this.noteStack.size(); i++) {
                if (!this.noteStack.get(i).on) {
                    this.noteStack.remove(i);
                    break;
                }
            }

            if (this.noteStack.size() == POLY_COUNT)
                this.noteStack.removeFirst();
        }

        this.noteStack.add(new MidiNote(note, velocity, true, channel, time));
    }

    public void stopNote(byte note, long time) {
        for (int i = 0; i < this.noteStack.size(); i++) {
            MidiNote midiNote = this.noteStack.get(i);
            if (midiNote.on && midiNote.note == note) this.noteStack.set(i, new MidiNote(
                    midiNote.note, midiNote.velocity, false, midiNote.channel, time, midiNote.withoutPrev()
            ));
        }
    }

    public void changeVelocity(int channel, byte velocity, long time) {
        for (int i = 0; i < this.noteStack.size(); i++) {
            MidiNote midiNote = this.noteStack.get(i);
            if (midiNote.on && midiNote.channel == channel) this.noteStack.set(i, new MidiNote(
                    midiNote.note, velocity, true, channel, time, midiNote.withoutPrev()
            ));
        }
    }

    public void setPitchBend(short pitchBend) {
        this.pitchBend = pitchBend != 0 ?
                (pitchBend >= 16384 ? pitchBend - 32767 : pitchBend) / 16384.0 :
                0.0;
    }

    public void close() {
        this.noteStack.clear();
        this.pitchBend = 0.0;
    }

    private boolean isPoly() {
        return this.synthBlockEntity.getBlockState().getValueOrElse(MidiInputBlock.POLYPHONIC, false);
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return (_, size) -> this.process(size, outPort);
    }

    private PolySampleSource process(int size, int outPort) {
        if (this.noteStack.isEmpty()) return new PolySampleSource(new double[size]);

        return this.isPoly() ?
                this.processPoly(size, outPort) :
                this.processMono(size, outPort);
    }

    private PolySampleSource processMono(int size, int outPort) {
        MidiNote playedNote = this.noteStack.getLast();
        for (MidiNote midiNote : this.noteStack)
            if (midiNote.on) playedNote = midiNote;

        MidiNote prevNote = null;
        for (MidiNote midiNote : this.noteStack)
            if (midiNote.on && midiNote != playedNote && midiNote.note != playedNote.note)
                prevNote = midiNote;

        double[] samples = new double[size];
        this.writeSamples(samples, outPort, playedNote, prevNote != null ? prevNote : playedNote.prev);

        return new PolySampleSource(samples);
    }

    private PolySampleSource processPoly(int size, int outPort) {
        double[][] polySamples = new double[POLY_COUNT][size];
        int processCount = Math.min(this.noteStack.size(), POLY_COUNT);
        for (int i = 0; i < processCount; i++)
            this.writeSamples(polySamples[i], outPort, this.noteStack.get(i));

        return new PolySampleSource(polySamples);
    }

    private void writeSamples(double[] samples, int outPort, MidiNote midiNote, @Nullable MidiNote prevNote) {
        int delay = (int) (midiNote.time - this.time);
        if (delay < 0) delay = 0;
        else delay = Math.min((int) (delay * (ModularSynths.SAMPLE_RATE / 1000.0)), samples.length);

        boolean hasPrev = prevNote != null;
        switch (outPort) {
            case 0:
                if (hasPrev) Arrays.fill(
                        samples,
                        0,
                        delay,
                        SynthesisFunctions.getDoubleFromNote(prevNote.note + 3.0 + this.pitchBend) // +3 to align with midi values
                );
                Arrays.fill(
                        samples,
                        delay,
                        samples.length,
                        SynthesisFunctions.getDoubleFromNote(midiNote.note + 3.0 + this.pitchBend) // +3 to align with midi values
                );
                break;
            case 1:
                if (hasPrev) Arrays.fill(samples, 0, delay, prevNote.on ? 1.0 : 0.0);
                Arrays.fill(samples, delay, samples.length, midiNote.on ? 1.0 : 0.0);
                break;
            default:
                if (hasPrev) Arrays.fill(samples, 0, delay, prevNote.velocity / 127.0);
                Arrays.fill(samples, delay, samples.length, midiNote.velocity / 127.0);
        }
    }

    private void writeSamples(double[] samples, int outPort, MidiNote midiNote) {
        this.writeSamples(samples, outPort, midiNote, midiNote.prev);
    }

    @Override
    public Runnable bufferCleanupTask() {
        return () -> this.time = System.currentTimeMillis();
    }

    private record MidiNote(byte note, byte velocity, boolean on, int channel, long time, @Nullable MidiNote prev) {
        private MidiNote(byte note, byte velocity, boolean on, int channel, long time) {
            this(note, velocity, on, channel, time, null);
        }

        private MidiNote withoutPrev() {
            return new MidiNote(this.note, this.velocity, this.on, this.channel, this.time, null);
        }
    }
}
