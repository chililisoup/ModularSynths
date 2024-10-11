package dev.chililisoup.modularsynths.client;

import javax.sound.midi.*;
import java.util.ArrayList;

public class MidiInput {
    private final ArrayList<MidiDevice> openDevices = new ArrayList<>();
    public final ArrayList<MidiNote> noteStack = new ArrayList<>();
    public final int polyCount = 8;

    public double pitchBend = 0.0;

    private long lastUpdateTime = System.currentTimeMillis();
    public long getUpdateTime() {
        long returnTime = lastUpdateTime;
        this.lastUpdateTime = System.currentTimeMillis();
        return returnTime;
    }

    public MidiInput() {
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                String name = device.getDeviceInfo().toString();

                for (Transmitter transmitter : device.getTransmitters()) {
                    transmitter.setReceiver(new MidiInputReceiver(name));
                }

                device.getTransmitter().setReceiver(new MidiInputReceiver(name));

                device.open();
                openDevices.add(device);

            } catch (MidiUnavailableException ignored) {}
        }
    }

    public void close() {
        openDevices.forEach(device -> {
            if (device.isOpen()) device.close();
        });
    }

    private class MidiInputReceiver implements Receiver {
        public final String name;

        public MidiInputReceiver(String name) {
            this.name = name;
        }

        private void stopNote(int noteValue, long time) {
            for (int i = 0; i < noteStack.size(); i++) {
                MidiNote note = noteStack.get(i);
                if (note.on && note.note == noteValue) {
                    noteStack.set(i, new MidiNote(note.note, note.velocity, false, note.channel, time, note));
                }
            }
        }

        @Override
        public void send(MidiMessage midiMessage, long timeStamp) {
            if (!(midiMessage instanceof ShortMessage message)) return;

            byte[] data = message.getMessage();
            int command = message.getCommand();
            int channel = message.getChannel();
            long time = System.currentTimeMillis();

            switch (command) {
                case ShortMessage.NOTE_ON:
                    stopNote(data[1], time);
                    noteStack.add(new MidiNote(data[1], data[2], true, channel, time, null));

                    if (noteStack.size() > polyCount) {
                        for (int i = 0; i < noteStack.size(); i++) {
                            if (!noteStack.get(i).on) {
                                noteStack.remove(i);
                                break;
                            }
                        }
                    }

                    if (noteStack.size() > polyCount) noteStack.remove(0);

                    break;
                case ShortMessage.NOTE_OFF:
                    stopNote(data[1], time);
                    break;
                case ShortMessage.CHANNEL_PRESSURE:
                    for (int i = 0; i < noteStack.size(); i++) {
                        MidiNote note = noteStack.get(i);
                        if (note.on && note.channel == channel) {
                            noteStack.set(i, new MidiNote(note.note, data[1], true, channel, time, note));
                        }
                    }

                    break;
                case ShortMessage.PITCH_BEND:
                    short val = (short) (((data[2] & 0xFF) << 8) | (data[1] & 0xFF));

                    if (val != 0) {
                        if (val >= 16384) val -= 32767;
                        pitchBend = (double) val / 16384;
                    } else pitchBend = 0.0;

                    break;
            }
        }

        @Override
        public void close() {}
    }

    public record MidiNote(byte note, byte velocity, boolean on, int channel, long time, MidiNote prev) {}
}
