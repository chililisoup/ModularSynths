package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;

public class EnvelopeBlock extends SynthBlock {
    public EnvelopeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getInputPositions() {
        return new Vec2[]{
                new Vec2(13F / 16F, 12F / 16F),
                new Vec2(14F / 16F, 3F / 16F),
                new Vec2(10F / 16F, 3F / 16F),
                new Vec2(6F / 16F, 3F / 16F),
                new Vec2(2F / 16F, 3F / 16F),
                new Vec2(13F / 16F, 8F / 16F)
        };
    }

    @Override
    public Vec2[] getOutputPositions() {
        return new Vec2[]{
                new Vec2(3F / 16F, 12F / 16F)
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestOutputData(double[][] inputStackSet, int size, int outputPort, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = inputStackSet[0];
        double[] controlStack = inputStackSet[5];

        ArrayList<Integer> envelopeData = blockEntity.getCustomIntData();
        if (envelopeData.isEmpty()) envelopeData.add(0);
        int envelopePosition = envelopeData.get(0);

        for (int i = 0; i < size; i++) {
            double attack = Math.abs(inputStackSet[1][i]) * ModularSynths.SAMPLE_RATE;
            double decay = Math.abs(inputStackSet[2][i]) * ModularSynths.SAMPLE_RATE;
            double sustain = Math.abs(inputStackSet[3][i]);
            double release = Math.abs(inputStackSet[4][i]) * ModularSynths.SAMPLE_RATE;

            if (controlStack[i] > 0.5 && envelopePosition < 0) envelopePosition = 0;
            if (controlStack[i] > 0.5 || envelopePosition < 0) envelopePosition++;
            if (controlStack[i] < 0.5 && envelopePosition > 0) envelopePosition = (int) Math.round(-release);

            if (envelopePosition == 0) {
                outputStack[i] = 0;
            } else if (envelopePosition < 0) {
                outputStack[i] *= (-envelopePosition * sustain) / release;
            } else if (envelopePosition < attack) {
                outputStack[i] *= 1.0 - ((attack - envelopePosition) / attack);
            } else if (envelopePosition < attack + decay)
                outputStack[i] *= ((decay - envelopePosition + attack) / decay) * (1.0 - sustain) + sustain;
            else outputStack[i] *= sustain;
        }

        envelopeData.set(0, envelopePosition);

        return outputStack;
    }
}
