package dev.chililisoup.modularsynths.util;

public enum EffectType {
    AMPLITUDE (SynthesisFunctions::amplitude);

    private final EffectType.EffectFunction effectFunction;

    EffectType(final EffectType.EffectFunction effectFunction) {
        this.effectFunction = effectFunction;
    }

    public double[] apply(double[] outputStack, double[] controlStack) {
        return this.effectFunction.get(outputStack, controlStack);
    }

    private interface EffectFunction {
        double[] get(double[] outputStack, double[] controlStack);
    }
}
