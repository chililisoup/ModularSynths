package dev.chililisoup.modularsynths.util;

public enum WaveType {
    SINE (SynthesisFunctions::sineWave),
    SQUARE (SynthesisFunctions::squareWave),
    TRIANGLE (SynthesisFunctions::triangleWave),
    SAWTOOTH (SynthesisFunctions::sawtoothWave);

    private final WaveFunction waveFunction;

    WaveType(final WaveFunction waveFunction) {
        this.waveFunction = waveFunction;
    }

    public short apply(int pos, double frequency, double amplitude) {
        return this.waveFunction.get(pos, frequency, amplitude);
    }

    private interface WaveFunction {
        short get(int pos, double frequency, double amplitude);
    }
}
