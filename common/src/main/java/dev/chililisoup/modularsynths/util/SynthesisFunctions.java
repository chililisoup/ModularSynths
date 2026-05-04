package dev.chililisoup.modularsynths.util;

import dev.chililisoup.modularsynths.ModularSynths;

import java.util.Random;

public abstract class SynthesisFunctions {
    private static final double LOG_2 = Math.log(2);
    private static final Random RANDOM = new Random();

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
        return Math.pow(2.0, (Math.log(frequency) / LOG_2) % 1.0 + octave);
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
        if (note > 11) text += " " + (1 + note / 12);
        return text;
    }

    public static double waveStep(double frequency) {
        return frequency / ModularSynths.SAMPLE_RATE;
    }

    public static double wavePeriod(double frequency) {
        return ModularSynths.SAMPLE_RATE / frequency;
    }

    public static double sineWave(double pos) {
        return Math.sin(2.0 * Math.PI * pos);
    }

    public static double squareWave(double pos) {
        return Math.signum(sineWave(pos));
    }

    public static double triangleWave(double pos) {
        return 4.0 * Math.abs(pos - Math.floor(0.5 + pos)) - 1;
    }

    public static double sawtoothWave(double pos) {
        return 2.0 * (pos - Math.floor(0.5 + pos));
    }

    public static double noiseWave() {
        return RANDOM.nextDouble();
    }

    public static double noiseWave(double ignoredPos) {
        return noiseWave();
    }

    public static double[] amplitude(double[] outputStack, double[] controlStack) {
        for (int i = 0; i < outputStack.length; i++)
            outputStack[i] *= controlStack[i];
        return outputStack;
    }

    public static double[] invert(double[] outputStack, double[] controlStack) {
        for (int i = 0; i < outputStack.length; i++) {
            outputStack[i] = Math.copySign((1.0 - Math.abs(outputStack[i])), outputStack[i])
                    * Math.abs(controlStack[i]) +
                    (1.0 - Math.abs(controlStack[i])) * outputStack[i];
        }
        return outputStack;
    }

    public static double[] polyToMono(double[][] dataSet, int size) {
        double[] monoStack = new double[size];

        for (double[] dataStack : dataSet) {
            for (int i = 0; i < size; i++) monoStack[i] += dataStack[i] / dataSet.length;
        }

        return monoStack;
    }
}
