package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.MidiInput;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PolyMidiBlock extends MonoMidiBlock {
    public PolyMidiBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[][] requestPolyData(HashMap<String, double[][]> inputStackSet, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        if (this.inputScreen == null) return new double[1][size];

        double[][] outputStackSet = new double[this.inputScreen.getPolyCount()][size];

        ArrayList<MidiInput.MidiNote> noteStack = this.inputScreen.getNoteStack();
        if (noteStack.isEmpty()) return outputStackSet;

        Direction face = state.getValue(FACING);

        for (int i = 0; i < noteStack.size(); i++) {
            double[] outputStack = outputStackSet[i];
            MidiInput.MidiNote note = noteStack.get(i);

            if (face.equals(outputDirection.getCounterClockWise())) {
                Arrays.fill(outputStack, SynthesisFunctions.getDoubleFromNote(note.note() + 3)); // +3 to align with midi values
            }

            if (face.equals(outputDirection)) {
                Arrays.fill(outputStack, note.on() ? 1.0 : 0.0);
            }

            if (face.equals(outputDirection.getClockWise())) {
                Arrays.fill(outputStack, (((double) note.velocity() + 0.5) / 255.0) + 0.5);
            }
        }

        return outputStackSet;
    }
}
