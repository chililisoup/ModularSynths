package dev.chililisoup.modularsynths.util;

import dev.chililisoup.modularsynths.ModularSynths;

public final class SynthesisFunctions {
    public static double getDoubleFromNote(double note) {
        return note / 64.0 - 1.0;
    }

    public static double getDoubleFromNote(int note) {
        return getDoubleFromNote((double) note);
    }

    public static double getFrequencyFromDouble(double value) {
        return Math.pow(2.0, ((1.0 + value) * 64.0 - 72.0) / 12.0) * 440.0;
    }

    public static double normalizeFrequency(double frequency, double octave) {
        return Math.pow(2.0, (Math.log(frequency) / ModUtil.LOG_TWO) % 1.0 + octave);
    }

    public static String getNoteName(int note) {
        String text = switch (note) {
            case 0, 12, 24 -> "F♯/G♭";
            case 1, 13 -> "G";
            case 2, 14 -> "G♯/A♭";
            case 3, 15 -> "A";
            case 4, 16 -> "A♯/B♭";
            case 5, 17 -> "B";
            case 6, 18 -> "C";
            case 7, 19 -> "C♯/D♭";
            case 8, 20 -> "D";
            case 9, 21 -> "D♯/E♭";
            case 10, 22 -> "E";
            case 11, 23 -> "F";
            default -> String.valueOf(note);
        };
        return note > 11 ? text + " " + (1 + note / 12) : text;
    }

    public static double waveStep(double frequency) {
        return frequency / ModularSynths.SAMPLE_RATE;
    }

    public static double wavePeriod(double frequency) {
        return ModularSynths.SAMPLE_RATE / frequency;
    }

    public static double sineWave(double phase) {
        return Math.sin(2.0 * Math.PI * phase);
    }

    public static double squareWave(double phase) {
        return Math.signum(sineWave(phase));
    }

    public static double triangleWave(double phase) {
        return 4.0 * Math.abs(phase - Math.floor(0.5 + phase)) - 1;
    }

    public static double sawtoothWave(double phase) {
        return 2.0 * (phase - Math.floor(0.5 + phase));
    }
}
