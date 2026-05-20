package dev.chililisoup.modularsynths.block.entity;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.AbstractSynthBlock;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.SynthInputConnection;
import dev.chililisoup.modularsynths.synthesis.SynthOutputConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SynthBlockEntity extends BlockEntity {
    public final AbstractSynth synth;

    public SynthBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SYNTH, pos, blockState);
        if (blockState.getBlock() instanceof AbstractSynthBlock<?> synthBlock) {
            this.synth = synthBlock.newSynth(this);
        } else {
            ModularSynths.LOGGER.error("Created a synth block entity where there's not a synth block! {}", pos);
            this.synth = new AbstractSynth(this) {};
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.synth.stop();
    }

    public void setChanged(Holder.Reference<GameEvent> event) {
        super.setChanged();
        if (this.level == null) return;
        if (event != null) this.level.gameEvent(event, this.worldPosition, GameEvent.Context.of(this.getBlockState()));
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void setChanged() {
        this.setChanged(GameEvent.BLOCK_ACTIVATE);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.synth.loadInputs(input.read("inputs", AbstractSynth.INPUTS_CODEC).orElse(List.of()));
        this.synth.loadOutputs(input.read("outputs", AbstractSynth.OUTPUTS_CODEC).orElse(List.of()));

        if (this.hasLevel() && ModularSynths.isClientSide()) Minecraft.getInstance().schedule(
                () -> this.synth.onLoad(this.getLevel())
        );
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        List<AbstractSynth.InPort> inputs = this.synth.getInputList();
        output.storeNullable("inputs", AbstractSynth.INPUTS_CODEC, inputs.isEmpty() ? null : inputs);
        List<AbstractSynth.OutPort> outputs = this.synth.getOutputList();
        output.storeNullable("outputs", AbstractSynth.OUTPUTS_CODEC, outputs.isEmpty() ? null : outputs);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state) {
        if (this.level == null) return;

        List<AbstractSynth.InPort> inputs = this.synth.getInputList();
        for (int inPort = 0; inPort < inputs.size(); inPort++) {
            for (SynthInputConnection connection : inputs.get(inPort).connections()) {
                Containers.dropItemStack(
                        this.level, pos.getX(), pos.getY(), pos.getZ(), connection.getItem()
                );

                if (this.level.getBlockEntity(connection.pos()) instanceof SynthBlockEntity synthBlockEntity)
                    synthBlockEntity.synth.removeOutput(pos, inPort, connection.outPort());
            }
        }

        List<AbstractSynth.OutPort> outputs = this.synth.getOutputList();
        for (int outPort = 0; outPort < outputs.size(); outPort++) {
            for (SynthOutputConnection connection : outputs.get(outPort).connections()) {
                if (!(this.level.getBlockEntity(connection.pos()) instanceof SynthBlockEntity synthBlockEntity))
                    continue;

                SynthInputConnection inputConnection = synthBlockEntity.synth.popInput(
                        pos, connection.inPort(), outPort
                );

                if (inputConnection != null) Containers.dropItemStack(
                        level, pos.getX(), pos.getY(), pos.getZ(), inputConnection.getItem()
                );
            }
        }
    }
}
