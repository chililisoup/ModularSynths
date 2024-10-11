package dev.chililisoup.modularsynths.block.entity;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.client.synthesis.AudioStreamSupplier;
import dev.chililisoup.modularsynths.client.synthesis.BaseSoundInstance;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioPlayer;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import dev.chililisoup.modularsynths.util.Cable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SynthBlockEntity extends BlockEntity {
    private final ArrayList<Cable.Connection> inputConnections = new ArrayList<>();
    private BaseSoundInstance soundInstance;

    @Environment(EnvType.CLIENT) private double samplePosition = 0.0;
    @Environment(EnvType.CLIENT) private final ArrayList<Integer> customIntData = new ArrayList<>();
    @Environment(EnvType.CLIENT) private final ArrayList<Double> customDoubleData = new ArrayList<>();
    @Environment(EnvType.CLIENT) private final ArrayList<double[]> savedStacks = new ArrayList<>();

    public SynthBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SYNTH.get(), pos, blockState);
    }

    @Environment(EnvType.CLIENT)
    private double[][] alignStackSets(double[][] smallStackSet, double[][] largeStackSet, int size) {
        double[][] newStackSet = new double[largeStackSet.length][size];
        System.arraycopy(smallStackSet, 0, newStackSet, 0, smallStackSet.length);
        return newStackSet;
    }

    @Environment(EnvType.CLIENT)
    private double[][] request(int size, int depth, boolean beginning, Direction outputDirection) {
        HashMap<String, double[][]> inputStack = new HashMap<>();
        Level level = this.getLevel();

        if (level != null && depth < ModularSynths.MAX_SYNTH_DEPTH && !this.inputConnections.isEmpty()) {
            this.inputConnections.forEach(connection -> {
                BlockEntity blockEntity = level.getBlockEntity(connection.position);
                if (!(blockEntity instanceof SynthBlockEntity)) return;

                double[][] inputStackSet = ((SynthBlockEntity) blockEntity).request(size, depth + 1, beginning, connection.outDirection);

                String direction = connection.inDirection.getName();
                inputStack.putIfAbsent(direction, new double[inputStackSet.length][size]);
                double[][] dataStackSet = inputStack.get(direction);

                if (dataStackSet.length < inputStackSet.length) {
                    double[][] newStackSet = alignStackSets(dataStackSet, inputStackSet, size);
                    inputStack.put(direction, newStackSet);
                    dataStackSet = inputStackSet;
                } else if (inputStackSet.length < dataStackSet.length) {
                    inputStackSet = alignStackSets(dataStackSet, inputStackSet, size);
                }

                for (int i = 0; i < dataStackSet.length; i++) {
                    for (int j = 0; j < size; j++) dataStackSet[i][j] += inputStackSet[i][j];
                }
            });
        }

        if (inputStack.isEmpty()) {
            double[][] defaultInput = new double[1][size];

            double inputFallback = ((SynthBlock) this.getBlockState().getBlock()).inputFallback();
            if (inputFallback != 0) Arrays.fill(defaultInput[0], inputFallback);

            inputStack.put("self", defaultInput);
        }

        if (beginning) {
            this.samplePosition = 0.0;
            this.customIntData.clear();
            this.customDoubleData.clear();
            this.savedStacks.clear();
        }
        return ((SynthBlock) this.getBlockState().getBlock()).requestPolyData(inputStack, outputDirection, size, this.getBlockState(), this);
    }

    @Environment(EnvType.CLIENT)
    public double[][] request(int size, boolean beginning) {
        return this.request(size, 0, beginning, null);
    }

    @Environment(EnvType.CLIENT)
    public void beginAudioStream() {
        Level level = this.getLevel();
        if (level == null) return;

        BlockPos pos = this.getBlockPos();

        this.soundInstance = SynthesizedAudioPlayer.playSound(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                new AudioStreamSupplier(this)
        );
    }

    @Environment(EnvType.CLIENT)
    public void endAudioStream() {
        if (this.soundInstance == null) return;

        this.soundInstance.stopStreaming();
        this.soundInstance = null;
    }

    public void findInputs(Level level, ArrayList<BlockPos> updatedPositions) {
        if (!((SynthBlock) this.getBlockState().getBlock()).acceptsInput()) return;

        this.inputConnections.clear();
        this.inputConnections.addAll(
                Cable.exploreFrom(this.getBlockPos(), level, updatedPositions)
                        .stream().filter(connection -> {
                            Block block = level.getBlockState(connection.position).getBlock();
                            if (!(block instanceof SynthBlock)) return false;
                            return ((SynthBlock) block).sendsOutput();
                        }).toList()
        );

        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void findInputs(Level level) {
        ArrayList<BlockPos> updatedPositions = new ArrayList<>();
        updatedPositions.add(this.getBlockPos());
        findInputs(level, updatedPositions);
    }

    @Environment(EnvType.CLIENT)
    public double getSamplePosition() {
        return samplePosition;
    }

    @Environment(EnvType.CLIENT)
    public void setSamplePosition(double samplePosition) {
        this.samplePosition = samplePosition;
    }

    @Environment(EnvType.CLIENT)
    public ArrayList<Integer> getCustomIntData() {
        return customIntData;
    }

    @Environment(EnvType.CLIENT)
    public ArrayList<Double> getCustomDoubleData() {
        return customDoubleData;
    }

    @Environment(EnvType.CLIENT)
    public ArrayList<double[]> getSavedStacks() {
        return savedStacks;
    }

    private CompoundTag prepareUpdateTag(CompoundTag tag) {
        ListTag savedConnections = new ListTag();
        savedConnections.addAll(inputConnections.stream().map(Cable.Connection::toTag).toList());

        tag.put("InputConnections", savedConnections);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        prepareUpdateTag(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.inputConnections.clear();
        this.inputConnections.addAll(tag.getList("InputConnections", 10).stream().map(connectionTag -> {
            CompoundTag compoundTag = (CompoundTag) connectionTag;
            return new Cable.Connection(compoundTag.getLong("pos"), compoundTag.getInt("inDir"), compoundTag.getInt("outDir"));
        }).toList());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return prepareUpdateTag(new CompoundTag());
    }
}
