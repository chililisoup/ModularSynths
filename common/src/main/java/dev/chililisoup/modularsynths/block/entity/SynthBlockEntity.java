package dev.chililisoup.modularsynths.block.entity;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import dev.chililisoup.modularsynths.util.CableExplorer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
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

public class SynthBlockEntity extends BlockEntity {
    private final ArrayList<BlockPos> inputBlocks = new ArrayList<>();

    public SynthBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SYNTH.get(), pos, blockState);
    }

    @Environment(EnvType.CLIENT)
    private short[] request(Level level, int size, int depth) {
        short[] inputStack = new short[size];

        if (depth < ModularSynths.MAX_SYNTH_DEPTH && !this.inputBlocks.isEmpty()) {
            this.inputBlocks.forEach(blockPos -> {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (!(blockEntity instanceof SynthBlockEntity)) return;

                short[] input = ((SynthBlockEntity) blockEntity).request(level, size, depth + 1);
                for (int i = 0; i < size; i++) {
                    inputStack[i] += input[i]; // this might need to be checked to prevent rollover?
                }
            });
        } else {
            short inputFallback = ((SynthBlock) this.getBlockState().getBlock()).inputFallback();
            if (inputFallback != 0) Arrays.fill(inputStack, inputFallback);
        }

        return ((SynthBlock) this.getBlockState().getBlock()).requestData(inputStack, this.getBlockState());
    }

    @Environment(EnvType.CLIENT)
    public short[] request(Level level, int size) {
        return this.request(level, size, 0);
    }

    public void findInputs(Level level) {
        if (!((SynthBlock) this.getBlockState().getBlock()).acceptsInput()) return;

        this.inputBlocks.clear();
        this.inputBlocks.addAll(
                CableExplorer.exploreFrom(this.getBlockPos(), level)
                        .stream().filter(blockPos -> {
                            Block block = level.getBlockState(blockPos).getBlock();
                            if (!(block instanceof SynthBlock)) return false;
                            return ((SynthBlock) block).sendsOutput();
                        }).toList()
        );

        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    private CompoundTag prepareUpdateTag(CompoundTag tag) {
        tag.put("InputBlocks", new LongArrayTag(
                inputBlocks.stream().mapToLong(BlockPos::asLong).toArray()
        ));

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

        this.inputBlocks.clear();
        this.inputBlocks.addAll(Arrays.stream(tag.getLongArray("InputBlocks")).mapToObj(BlockPos::of).toList());
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
