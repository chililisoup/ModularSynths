package dev.chililisoup.modularsynths.util;

public enum EffectType {
    AMPLITUDE (SynthesisFunctions::amplitude);

    private final EffectType.EffectFunction effectFunction;

    EffectType(final EffectType.EffectFunction effectFunction) {
        this.effectFunction = effectFunction;
    }

    public short[] apply(short[] outputStack, short[] controlStack) {
        return this.effectFunction.get(outputStack, controlStack);
    }

    private interface EffectFunction {
        short[] get(short[] outputStack, short[] controlStack);
    }
}
