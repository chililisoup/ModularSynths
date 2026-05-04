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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class MonoMidiBlock extends SynthBlock {
    @Environment(EnvType.CLIENT) public MidiInputScreen inputScreen;

    public MonoMidiBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec2[] getOutputPositions() {
        return new Vec2[]{
                new Vec2(3F / 16F, 4F / 16F),
                new Vec2(8F / 16F, 4F / 16F),
                new Vec2(13F / 16F, 4F / 16F)
        };
    }

    @Environment(EnvType.CLIENT)
    protected void writeOutput(double[] outputStack, int outputPort, MidiInput.MidiNote note, double pitchBend, long time) {
        int delay = (int) (note.time() - time);

        if (delay < 0) delay = 0;
        else delay = Math.min((int) ((double) delay * (ModularSynths.SAMPLE_RATE / 1000.0)), outputStack.length);

        MidiInput.MidiNote prev = note.prev();
        boolean hasPrev = prev != null;

        switch (outputPort) {
            case 0:
                if (hasPrev) Arrays.fill(
                        outputStack,
                        0,
                        delay,
                        SynthesisFunctions.getDoubleFromNote((double) prev.note() + 3.0 + pitchBend) // +3 to align with midi values
                );
                Arrays.fill(
                        outputStack,
                        delay,
                        outputStack.length,
                        SynthesisFunctions.getDoubleFromNote((double) note.note() + 3.0 + pitchBend) // +3 to align with midi values
                );
                break;
            case 1:
                if (hasPrev) Arrays.fill(outputStack, 0, delay, prev.on() ? 1.0 : 0.0);
                Arrays.fill(outputStack, delay, outputStack.length, note.on() ? 1.0 : 0.0);
                break;
            default:
                if (hasPrev) Arrays.fill(outputStack, 0, delay, (double) prev.velocity() / 127.0);
                Arrays.fill(outputStack, delay, outputStack.length, (double) note.velocity() / 127.0);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public double[] requestOutputData(double[][] inputStackSet, int size, int outputPort, BlockState state, SynthBlockEntity blockEntity) {
        double[] outputStack = new double[size];

        if (this.inputScreen == null) return outputStack;

        long time = this.inputScreen.getUpdateTime();

        ArrayList<MidiInput.MidiNote> noteStack = this.inputScreen.getNoteStack();
        if (noteStack.isEmpty()) return outputStack;

        MidiInput.MidiNote note = noteStack.get(noteStack.size() - 1);
        for (MidiInput.MidiNote midiNote : noteStack) {
            if (midiNote.on()) note = midiNote;
        }

        this.writeOutput(outputStack, outputPort, note, this.inputScreen.getPitchBend(), time);

        return outputStack;
    }

    @Environment(EnvType.CLIENT)
    private void openScreen(Level level, BlockPos pos) {
        this.inputScreen = new MidiInputScreen(level, pos);
        Minecraft.getInstance().setScreen(this.inputScreen);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) return InteractionResult.SUCCESS;

        this.openScreen(level, pos);
        return InteractionResult.CONSUME;
    }
}
