package dev.chililisoup.modularsynths.network;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.reg.ModPayloadTypes;
import dev.chililisoup.modularsynths.synthesis.modules.MidiInputSynth;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record ClientboundMidiInputPayload(
        BlockPos pos, long time, double pitchBend, List<MidiInputSynth.NetworkedMidiNote> noteStack
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMidiInputPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundMidiInputPayload::pos,
            ByteBufCodecs.LONG, ClientboundMidiInputPayload::time,
            ByteBufCodecs.DOUBLE, ClientboundMidiInputPayload::pitchBend,
            MidiInputSynth.NetworkedMidiNote.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundMidiInputPayload::noteStack,
            ClientboundMidiInputPayload::new
    );

    public static ClientboundMidiInputPayload of(ServerboundMidiInputPayload payload) {
        return new ClientboundMidiInputPayload(
                payload.pos(), payload.time(), payload.pitchBend(), payload.noteStack()
        );
    }

    @Override
    public @NonNull Type<ClientboundMidiInputPayload> type() {
        return ModPayloadTypes.CLIENTBOUND_MIDI_INPUT;
    }

    public static void handle(ClientboundMidiInputPayload payload, ClientPlayNetworking.Context context) {
        if (!(context.player().level().getBlockEntity(payload.pos) instanceof SynthBlockEntity blockEntity))
            return;

        if (blockEntity.synth instanceof MidiInputSynth synth)
            synth.updateMidiInputClient(payload.time, payload.pitchBend, payload.noteStack);
    }
}
