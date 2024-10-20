package dev.chililisoup.modularsynths.util;

import dev.chililisoup.modularsynths.ModularSynths;

import java.util.Random;

public abstract class SynthesisFunctions {
    private static final Random random = new Random();

    public static double getDoubleFromNote(double note) {
        return (1.0 / 64.0) * note - 1.0;
    }

    public static double getDoubleFromNote(int note) {
        return getDoubleFromNote((double) note);
    }

    public static double getFrequencyFromDouble(double value) {
        return Math.pow(2.0, ((1.0 + value) * 64.0 - 72.0) / 12.0) * 440.0;
    }

    public static double waveStep(double frequency) {
        return frequency / ModularSynths.SAMPLE_RATE;
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
        return random.nextDouble();
    }

    public static double noiseWave(double ignoredPos) {
        return noiseWave();
    }

    public static double[] amplitude(double[] outputStack, double[] controlStack) {
        for (int i = 0; i < outputStack.length; i++) {
            outputStack[i] = outputStack[i] * controlStack[i];
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
