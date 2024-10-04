package dev.chililisoup.modularsynths.util;

public enum EffectType {
    AMPLITUDE (SynthesisFunctions::amplitude);

    private final EffectType.EffectFunction effectFunction;

    EffectType(final EffectType.EffectFunction effectFunction) {
        this.effectFunction = effectFunction;
    }

    public short apply(double value, double amount) {
        return this.effectFunction.get(value, amount);
    }

    private interface EffectFunction {
        short get(double value, double amount);
    }
}
