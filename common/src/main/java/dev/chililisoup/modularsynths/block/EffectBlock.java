package dev.chililisoup.modularsynths.block;

public class EffectBlock extends SynthBlock {
    public EffectBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean sendsOutput() {
        return true;
    }

    @Override
    public boolean acceptsInput() {
        return true;
    }
}
