package dev.chililisoup.modularsynths.synthesis.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.MidiInputBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class MidiInputSynth extends AbstractSynth {
    private final @Nullable MidiNote[] noteStack = new MidiNote[8];
    private long lastReceivedTime = 0;
    private long time = System.currentTimeMillis();
    private double pitchBend = 0.0;
    private @Nullable UUID controllingPlayer;

    public MidiInputSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public void setControllingPlayer(@Nullable UUID playerUUID) {
        this.controllingPlayer = playerUUID;
        if (this.controllingPlayer == null) this.close();
    }

    public @Nullable UUID getControllingPlayer() {
        return this.controllingPlayer;
    }

    public boolean playCanNoLongerControl() {
        if (this.controllingPlayer == null) return false;
        Level level = this.synthBlockEntity.getLevel();
        if (level == null) return true;
        Player controllingPlayer = level.getPlayerByUUID(this.controllingPlayer);
        return controllingPlayer == null || !controllingPlayer.isWithinBlockInteractionRange(
                this.synthBlockEntity.getBlockPos(), 4.0
        );
    }

    private void clearControllingPlayerIfInvalid() {
        if (this.playCanNoLongerControl()) this.setControllingPlayer(null);
    }

    public static void tick(SynthBlockEntity synthBlockEntity) {
        if (synthBlockEntity.synth instanceof MidiInputSynth synth)
            synth.clearControllingPlayerIfInvalid();
    }

    private int newestNoteIndex(int except) {
        int index = -1;
        long time = -1;
        boolean on = false;

        for (int i = 0; i < this.noteStack.length; i++) {
            if (i == except) continue;

            MidiNote midiNote = this.noteStack[i];
            if (midiNote == null) continue;
            if (index < 0) {
                index = i;
                time = midiNote.time;
                on = midiNote.on;
                continue;
            }
            if (on != midiNote.on) {
                if (on) continue;
                index = i;
                time = midiNote.time;
                on = true;
                continue;
            }
            if (midiNote.time > time) {
                index = i;
                time = midiNote.time;
            }
        }

        return index;
    }

    private int newestNoteIndex() {
        return this.newestNoteIndex(-1);
    }

    private int oldestNoteIndex() {
        int index = -1;
        long time = Long.MAX_VALUE;
        boolean on = true;

        for (int i = 0; i < this.noteStack.length; i++) {
            MidiNote midiNote = this.noteStack[i];
            if (midiNote == null) return i;
            if (index < 0) {
                index = i;
                time = midiNote.time;
                on = midiNote.on;
                continue;
            }
            if (on != midiNote.on) {
                if (!on) continue;
                index = i;
                time = midiNote.time;
                on = false;
                continue;
            }
            if (midiNote.time < time) {
                index = i;
                time = midiNote.time;
            }
        }

        return Math.max(index, 0);
    }

    public void addNote(byte note, byte velocity, int channel, long time) {
        this.removeNote(note);
        this.noteStack[this.oldestNoteIndex()] = new MidiNote(note, velocity, true, channel, time);
    }

    public void removeNote(byte note) {
        for (int i = 0; i < this.noteStack.length; i++) {
            MidiNote midiNote = this.noteStack[i];
            if (midiNote != null && midiNote.note == note)
                this.noteStack[i] = null;
        }
    }

    public void stopNote(byte note, long time) {
        MidiNote removedNote = null;
        int index = 0;
        for (int i = 0; i < this.noteStack.length; i++) {
            MidiNote midiNote = this.noteStack[i];
            if (midiNote != null && midiNote.note == note) {
                this.noteStack[i] = null;
                removedNote = midiNote;
                index = i;
            }
        }

        if (removedNote != null) this.noteStack[index] = new MidiNote(
                removedNote.note, removedNote.velocity, false, removedNote.channel, time
        );
    }

    public void changeVelocity(int channel, byte velocity) {
        MidiNote removedNote = null;
        int index = 0;
        for (int i = 0; i < this.noteStack.length; i++) {
            MidiNote midiNote = this.noteStack[i];
            if (midiNote != null && midiNote.on && midiNote.channel == channel) {
                this.noteStack[i] = null;
                removedNote = midiNote;
                index = i;
            }
        }

        if (removedNote != null) this.noteStack[index] = new MidiNote(
                removedNote.note, velocity, true, channel, removedNote.time
        );
    }

    public void setPitchBend(short pitchBend) {
        this.pitchBend = pitchBend != 0 ?
                (pitchBend >= 16384 ? pitchBend - 32767 : pitchBend) / 16384.0 :
                0.0;
    }

    public double getPitchBend() {
        return this.pitchBend;
    }

    public List<NetworkedMidiNote> getNetworkedNoteStack() {
        return Arrays.stream(this.noteStack).anyMatch(Objects::nonNull) ?
                Arrays.stream(this.noteStack).map(NetworkedMidiNote::new).toList() :
                List.of();
    }

    public boolean updateMidiInputServer(Player player, long time, List<NetworkedMidiNote> noteStack) {
        if (!player.getUUID().equals(this.controllingPlayer)) return false;
        if (noteStack.size() != this.noteStack.length) {
            this.setControllingPlayer(null);
            this.close();
            return true;
        }

        if (this.lastReceivedTime > time) return false;
        this.lastReceivedTime = time;
        return true;
    }

    public void updateMidiInputClient(long time, double pitchBend, List<NetworkedMidiNote> noteStack) {
        this.pitchBend = pitchBend;
        long offset = this.time - time;
        for (int i = 0; i < noteStack.size(); i++)
            this.noteStack[i] = noteStack.get(i).midiNote
                    .map(midiNote -> midiNote.withOffset(offset))
                    .orElse(null);
    }

    public void close() {
        Arrays.fill(this.noteStack, null);
        this.pitchBend = 0.0;
        this.lastReceivedTime = 0;
    }

    @Override
    public void powerOff() {
        this.close();
    }

    private boolean isPoly() {
        return this.synthBlockEntity.getBlockState().getValueOrElse(MidiInputBlock.POLYPHONIC, false);
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return (_, size) -> this.process(size, outPort);
    }

    private PolySampleSource process(int size, int outPort) {
        if (Arrays.stream(this.noteStack).allMatch(Objects::isNull))
            return new PolySampleSource(new double[size]);

        return this.isPoly() ?
                this.processPoly(size, outPort) :
                this.processMono(size, outPort);
    }

    private PolySampleSource processMono(int size, int outPort) {
        int index = this.newestNoteIndex();
        if (index < 0) return new PolySampleSource(new double[size]);

        MidiNote playedNote = this.noteStack[index];
        if (playedNote == null) return new PolySampleSource(new double[size]);

        int prevIndex = playedNote.on ? this.newestNoteIndex(index) : -1;
        MidiNote prevNote = prevIndex < 0 ? null : this.noteStack[index];

        double[] samples = new double[size];
        this.writeSamples(samples, outPort, playedNote, playedNote.on ? prevNote : new MidiNote(
                playedNote.note, playedNote.velocity, true, playedNote.channel, 0
        ));

        return new PolySampleSource(samples);
    }

    private PolySampleSource processPoly(int size, int outPort) {
        double[][] polySamples = new double[this.noteStack.length][size];

        for (int i = 0; i < this.noteStack.length; i++) {
            MidiNote midiNote = this.noteStack[i];
            if (midiNote != null) this.writeSamples(polySamples[i], outPort, midiNote, null);
        }

        return new PolySampleSource(polySamples);
    }

    private void writeSamples(double[] samples, int outPort, MidiNote midiNote, @Nullable MidiNote prevNote) {
        int delay = (int) (midiNote.time - this.time);
        if (delay < 0) delay = 0;
        else delay = Math.min((int) (delay * (ModularSynths.SAMPLE_RATE / 1000.0)), samples.length);

        if (prevNote == null && !midiNote.on) prevNote = new MidiNote(
                midiNote.note, midiNote.velocity, true, midiNote.channel, 0
        );

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

    @Override
    public Runnable bufferCleanupTask() {
        return () -> this.time = System.currentTimeMillis();
    }

    public record NetworkedMidiNote(Optional<MidiNote> midiNote) {
        public static final MapCodec<NetworkedMidiNote> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                MidiNote.CODEC.codec().optionalFieldOf("midiNote").forGetter(NetworkedMidiNote::midiNote)
        ).apply(i, NetworkedMidiNote::new));

        public static final StreamCodec<ByteBuf, NetworkedMidiNote> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC.codec());

        public NetworkedMidiNote(@Nullable MidiNote midiNote) {
            this(Optional.ofNullable(midiNote));
        }
    }

    public record MidiNote(byte note, byte velocity, boolean on, int channel, long time) {
        public static final MapCodec<MidiNote> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.BYTE.fieldOf("note").forGetter(MidiNote::note),
                Codec.BYTE.fieldOf("velocity").forGetter(MidiNote::velocity),
                Codec.BOOL.fieldOf("on").forGetter(MidiNote::on),
                Codec.INT.fieldOf("channel").forGetter(MidiNote::channel),
                Codec.LONG.fieldOf("time").forGetter(MidiNote::time)
        ).apply(i, MidiNote::new));

        public MidiNote withOffset(long offset) {
            return new MidiNote(this.note, this.velocity, this.on, this.channel, this.time + offset);
        }
    }
}
