package dev.chililisoup.modularsynths.block;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.MidiInput;
import dev.chililisoup.modularsynths.gui.MidiInputScreen;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MonoMidiBlock extends SynthBlock {
    @Environment(EnvType.CLIENT)
    public MidiInputScreen inputScreen;

    public MonoMidiBlock(Properties properties) {
        super(properties);
    }

    @Environment(EnvType.CLIENT)
    protected void writeOutput(double[] outputStack, Direction outputDirection, Direction face, MidiInput.MidiNote note, double pitchBend, long time) {
        int delay = (int) (note.time() - time);

        if (delay < 0) delay = 0;
        else delay = Math.min((int) ((double) delay * (ModularSynths.SAMPLE_RATE / 1000.0)), outputStack.length);

        if (face.equals(outputDirection.getCounterClockWise())) {
            MidiInput.MidiNote prev = note.prev();
            if (prev != null) {
                Arrays.fill(
                        outputStack,
                        0,
                        delay,
                        SynthesisFunctions.getDoubleFromNote((double) prev.note() + 3.0 + pitchBend) // +3 to align with midi values
                );
            }

            Arrays.fill(
                    outputStack,
                    delay,
                    outputStack.length,
                    SynthesisFunctions.getDoubleFromNote((double) note.note() + 3.0 + pitchBend) // +3 to align with midi values
            );
        } else if (face.equals(outputDirection)) {
            MidiInput.MidiNote prev = note.prev();
            if (prev != null) {
                Arrays.fill(outputStack, delay, outputStack.length, prev.on() ? 1.0 : 0.0);
            }
            Arrays.fill(outputStack, delay, outputStack.length, note.on() ? 1.0 : 0.0);
        } else if (face.equals(outputDirection.getClockWise())) {
            MidiInput.MidiNote prev = note.prev();
            if (prev != null) {
                Arrays.fill(outputStack, delay, outputStack.length, (double) prev.velocity() / 127.0);
            }
            Arrays.fill(outputStack, delay, outputStack.length, (double) note.velocity() / 127.0);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = new double[size];

        if (this.inputScreen == null) return outputStack;

        long time = this.inputScreen.getUpdateTime();

        ArrayList<MidiInput.MidiNote> noteStack = this.inputScreen.getNoteStack();
        if (noteStack.isEmpty()) return outputStack;

        Direction face = state.getValue(FACING);
        MidiInput.MidiNote note = noteStack.get(noteStack.size() - 1);
        for (MidiInput.MidiNote midiNote : noteStack) {
            if (midiNote.on()) note = midiNote;
        }

        this.writeOutput(outputStack, outputDirection, face, note, this.inputScreen.getPitchBend(), time);

        return outputStack;
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) return InteractionResult.SUCCESS;

        this.inputScreen = new MidiInputScreen(level, pos);
        Minecraft.getInstance().setScreen(this.inputScreen);

        return InteractionResult.CONSUME;
    }

    @Override
    public Direction[] getOutputs(BlockState state) {
        Direction face = state.getValue(FACING);

        if (face.equals(Direction.UP) || face.equals(Direction.DOWN))
            return new Direction[]{face};
        else return new Direction[]{
                face,
                face.getClockWise(),
                face.getCounterClockWise()
        };
    }

    @Override
    public boolean sendsOutput() {
        return true;
    }

    @Override
    public boolean acceptsInput() {
        return false;
    }
}
