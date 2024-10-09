package dev.chililisoup.modularsynths.client;

import javax.sound.midi.*;
import java.util.ArrayList;

public class MidiInput {
    private final ArrayList<MidiDevice> openDevices = new ArrayList<>();
    public final ArrayList<MidiNote> noteStack = new ArrayList<>();
    public final int polyCount = 8;

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

        private void stopNote(int noteValue) {
            for (int i = 0; i < noteStack.size(); i++) {
                MidiNote note = noteStack.get(i);
                if (note.on && note.note == noteValue) {
                    noteStack.set(i, new MidiNote(note.note, note.velocity, false));
                }
            }
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            byte[] data = message.getMessage();

            if (data[0] == -112) {
                stopNote(data[1]);
                noteStack.add(new MidiNote(data[1], data[2], true));

                if (noteStack.size() > polyCount) {
                    for (int i = 0; i < noteStack.size(); i++) {
                        if (!noteStack.get(i).on) {
                            noteStack.remove(i);
                            break;
                        }
                    }
                }

                if (noteStack.size() > polyCount) noteStack.remove(0);

            } else if (data[0] == -128) {
                stopNote(data[1]);
            }
        }

        @Override
        public void close() {}
    }

    public record MidiNote(byte note, byte velocity, boolean on) {}
}
