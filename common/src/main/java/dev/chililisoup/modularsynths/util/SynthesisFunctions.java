package dev.chililisoup.modularsynths.util;

import dev.chililisoup.modularsynths.ModularSynths;
import net.minecraft.util.Mth;

public abstract class SynthesisFunctions {
    public static double remapShort(short value) {
        return ((double) value + 32768.0) / 65535.0;
    }

    private static double convertFrequency(double frequency) {
        return (2.0 * Math.PI * frequency) / ModularSynths.SAMPLE_RATE;
    }

    public static double getFrequencyFromNote(int note) { // 72 = A @ 440 Hz
        return Math.pow(2.0, ((double) note - 72.0) / 12.0) * 440.0;
    }

    public static short getShortFromNote(int note) { // maps 0 - 127 to -32768 - 32767
        return (short) ((65535.0 / 127.0) * note - 32768.0);
    }

    public static int getNoteFromShort(short value) { // maps -32768 - 32767 to 0 - 127
        return (int) Math.round((32768.0 + value) * (127.0 / 65535.0));
    }

    public static double getFrequencyFromShort(short value) {
        return Math.pow(2.0, ((32768.0 + (double) value) * (127.0 / 65535.0) - 72.0) / 12.0) * 440.0;
    }

    public static short sineWave(int pos, double frequency, double amplitude) {
        return (short) (amplitude * Short.MAX_VALUE * Math.sin(convertFrequency(frequency) * pos));
    }

    public static short squareWave(int pos, int quality, double frequency, double amplitude) {
        frequency = convertFrequency(frequency);

        double sum = 0.0;
        for (int i = 0; i < quality; i++) {
            sum += Math.sin((2.0 * i + 1.0) * frequency * pos) / (2.0 * i + 1.0);
        }

        return (short) (amplitude * Short.MAX_VALUE * sum);
    }

    public static short squareWave(int pos, double frequency, double amplitude) {
        return squareWave(pos, 10, frequency, amplitude);
    }

    public static short triangleWave(int pos, double frequency, double amplitude) {
        frequency = ModularSynths.SAMPLE_RATE / frequency;
        return (short) (amplitude * Short.MAX_VALUE * (4.0 * Math.abs(pos % frequency - (frequency / 2.0)) / frequency - 1.0));
    }

    public static short sawtoothWave(int pos, double frequency, double amplitude) {
        return (short) (amplitude * Short.MAX_VALUE * (2.0 * ((pos * frequency / ModularSynths.SAMPLE_RATE) % 1.0)) - 1.0);
    }

    public static short[] amplitude(short[] outputStack, short[] controlStack) {
        for (int i = 0; i < outputStack.length; i++) {
            outputStack[i] = (short) Mth.clamp(
                    Math.round((double) outputStack[i] * remapShort(controlStack[i])),
                    Short.MIN_VALUE,
                    Short.MAX_VALUE
            );
        }
        return outputStack;
    }
}
