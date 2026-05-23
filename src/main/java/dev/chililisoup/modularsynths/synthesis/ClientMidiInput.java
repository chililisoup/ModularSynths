package dev.chililisoup.modularsynths.synthesis;

import dev.chililisoup.modularsynths.synthesis.modules.MidiInputSynth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.sound.midi.*;
import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ClientMidiInput {
    private final MidiInputSynth synth;
    private final ArrayList<MidiDevice> openDevices = new ArrayList<>();

    public ClientMidiInput(MidiInputSynth synth) {
        this.synth = synth;

        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                String name = device.getDeviceInfo().toString();

                for (Transmitter transmitter : device.getTransmitters())
                    transmitter.setReceiver(new MidiInputReceiver(name));

                device.getTransmitter().setReceiver(new MidiInputReceiver(name));

                device.open();
                openDevices.add(device);

            } catch (MidiUnavailableException ignored) {}
        }
    }

    public boolean stillValid() {
        return !this.synth.synthBlockEntity.isRemoved();
    }

    public void close() {
        this.openDevices.forEach(device -> {
            if (device.isOpen()) device.close();
        });
        this.synth.close();
    }

    private class MidiInputReceiver implements Receiver {
        public final String name;

        public MidiInputReceiver(String name) {
            this.name = name;
        }

        @Override
        public void send(MidiMessage midiMessage, long timeStamp) {
            if (!(midiMessage instanceof ShortMessage message)) return;

            byte[] data = message.getMessage();
            int command = message.getCommand();
            int channel = message.getChannel();
            long time = System.currentTimeMillis();

            switch (command) {
                case ShortMessage.NOTE_ON -> ClientMidiInput.this.synth
                        .addNote(data[1], data[2], channel, time);

                case ShortMessage.NOTE_OFF -> ClientMidiInput.this.synth
                        .stopNote(data[1], time);

                case ShortMessage.CHANNEL_PRESSURE -> ClientMidiInput.this.synth
                        .changeVelocity(channel, data[1], time);

                case ShortMessage.PITCH_BEND -> ClientMidiInput.this.synth
                        .setPitchBend((short) (((data[2] & 0xFF) << 8) | (data[1] & 0xFF)));
            }
        }

        @Override
        public void close() {}
    }
}
