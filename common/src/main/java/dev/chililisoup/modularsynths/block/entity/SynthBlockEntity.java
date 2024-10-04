package dev.chililisoup.modularsynths.block.entity;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import dev.chililisoup.modularsynths.util.Cable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

public class SynthBlockEntity extends BlockEntity {
    private final ArrayList<Cable.Connection> inputConnections = new ArrayList<>();

    public SynthBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SYNTH.get(), pos, blockState);
    }

    @Environment(EnvType.CLIENT)
    private short[] request(Level level, int size, int depth) {
        HashMap<String, short[]> inputStack = new HashMap<>();

        if (depth < ModularSynths.MAX_SYNTH_DEPTH && !this.inputConnections.isEmpty()) {
            this.inputConnections.forEach(connection -> {
                BlockEntity blockEntity = level.getBlockEntity(connection.position);
                if (!(blockEntity instanceof SynthBlockEntity)) return;

                String direction = connection.direction.getName();

                inputStack.putIfAbsent(direction, new short[size]);
                short[] stack = inputStack.get(direction);

                short[] input = ((SynthBlockEntity) blockEntity).request(level, size, depth + 1);
                IntStream.range(0, size).forEach(i ->
                        stack[i] = (short) Mth.clamp(
                                (int) stack[i] + (int) input[i],
                                Short.MIN_VALUE,
                                Short.MAX_VALUE
                        )
                );
            });
        } else {
            short[] defaultInput = new short[size];

            short inputFallback = ((SynthBlock) this.getBlockState().getBlock()).inputFallback();
            if (inputFallback != 0) Arrays.fill(defaultInput, inputFallback);

            inputStack.put("self", defaultInput);
        }

        return ((SynthBlock) this.getBlockState().getBlock()).requestData(inputStack, size, this.getBlockState());
    }

    @Environment(EnvType.CLIENT)
    public short[] request(Level level, int size) {
        return this.request(level, size, 0);
    }

    public void findInputs(Level level) {
        if (!((SynthBlock) this.getBlockState().getBlock()).acceptsInput()) return;

        this.inputConnections.clear();
        this.inputConnections.addAll(
                Cable.exploreFrom(this.getBlockPos(), level)
                        .stream().filter(connection -> {
                            Block block = level.getBlockState(connection.position).getBlock();
                            if (!(block instanceof SynthBlock)) return false;
                            return ((SynthBlock) block).sendsOutput();
                        }).toList()
        );

        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
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
            return new Cable.Connection(compoundTag.getLong("pos"), compoundTag.getInt("dir"));
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
