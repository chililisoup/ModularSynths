package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.MidiInput;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;

public class PolyMidiBlock extends MonoMidiBlock {
    public PolyMidiBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[][] requestPolyData(HashMap<String, double[][]> inputStackSet, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        if (this.inputScreen == null) return new double[1][size];

        int polyCount = this.inputScreen.getPolyCount();

        double[][] outputStackSet = new double[polyCount][size];
        long time = this.inputScreen.getUpdateTime();

        ArrayList<MidiInput.MidiNote> noteStack = this.inputScreen.getNoteStack();
        if (noteStack.isEmpty()) return outputStackSet;

        Direction face = state.getValue(FACING);
        double pitchBend = this.inputScreen.getPitchBend();

        for (int i = 0; i < Math.min(noteStack.size(), polyCount); i++) {
            this.writeOutput(outputStackSet[i], outputDirection, face, noteStack.get(i), pitchBend, time);
        }

        return outputStackSet;
    }
}
