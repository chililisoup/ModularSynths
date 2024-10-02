package dev.chililisoup.modularsynths.client;

import dev.chililisoup.modularsynths.ModularSynths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class WaveFunctions {
    private static double convertFrequency(double frequency) {
        return (2.0 * Math.PI * frequency) / ModularSynths.SAMPLE_RATE;
    }

    public static double getFrequencyFromNote(int note) {
        return Math.pow(2.0, ((double) note + 90.3763165623) / 12.0);
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

    public static short triangleWave(int pos, double frequency, double amplitude) {
        frequency = ModularSynths.SAMPLE_RATE / frequency;
        return (short) (amplitude * Short.MAX_VALUE * (4.0 * Math.abs(pos % frequency - (frequency / 2.0)) / frequency - 1.0));
    }

    public static short sawtoothWave(int pos, double frequency, double amplitude) {
        return (short) (amplitude * Short.MAX_VALUE * (2.0 * ((pos * frequency / ModularSynths.SAMPLE_RATE) % 1.0)) - 1.0);
    }
}
