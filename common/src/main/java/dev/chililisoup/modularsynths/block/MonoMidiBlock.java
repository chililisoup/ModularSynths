package dev.chililisoup.modularsynths.block;

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

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestData(HashMap<String, double[]> inputStack, Direction outputDirection, int size, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = new double[size];

        if (this.inputScreen == null) return outputStack;
        ArrayList<MidiInput.MidiNote> noteStack = this.inputScreen.getNoteStack();
        if (noteStack.isEmpty()) return outputStack;

        Direction face = state.getValue(FACING);
        MidiInput.MidiNote note = noteStack.get(noteStack.size() - 1);
        for (MidiInput.MidiNote midiNote : noteStack) {
            if (midiNote.on()) note = midiNote;
        }

        if (face.equals(outputDirection.getCounterClockWise())) {
            Arrays.fill(outputStack, SynthesisFunctions.getDoubleFromNote(note.note() + 3)); // +3 to align with midi values
        }

        if (face.equals(outputDirection)) {
            Arrays.fill(outputStack, note.on() ? 1.0 : 0.0);
        }

        if (face.equals(outputDirection.getClockWise())) {
            Arrays.fill(outputStack, (((double) note.velocity() + 0.5) / 255.0) + 0.5);
        }

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
