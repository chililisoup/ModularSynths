package dev.chililisoup.modularsynths.util;

public enum WaveType {
    SINE (SynthesisFunctions::sineWave),
    SQUARE (SynthesisFunctions::squareWave),
    TRIANGLE (SynthesisFunctions::triangleWave),
    SAWTOOTH (SynthesisFunctions::sawtoothWave),
    NOISE (SynthesisFunctions::noiseWave);

    private final WaveFunction waveFunction;

    WaveType(final WaveFunction waveFunction) {
        this.waveFunction = waveFunction;
    }

    public double apply(double pos) {
        return this.waveFunction.get(pos);
    }

    private interface WaveFunction {
        double get(double pos);
    }
}
