package dev.chililisoup.modularsynths.network;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.reg.ModPayloadTypes;
import dev.chililisoup.modularsynths.synthesis.modules.MidiInputSynth;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record ServerboundMidiInputPayload(
        BlockPos pos, long time, double pitchBend, List<MidiInputSynth.NetworkedMidiNote> noteStack
) implements CustomPacketPayload {
    private static final int BROADCAST_RANGE_SQR = 32 * 32;
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMidiInputPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerboundMidiInputPayload::pos,
            ByteBufCodecs.LONG, ServerboundMidiInputPayload::time,
            ByteBufCodecs.DOUBLE, ServerboundMidiInputPayload::pitchBend,
            MidiInputSynth.NetworkedMidiNote.STREAM_CODEC.apply(ByteBufCodecs.list()), ServerboundMidiInputPayload::noteStack,
            ServerboundMidiInputPayload::new
    );

    public static ServerboundMidiInputPayload of(MidiInputSynth synth, long time) {
        return new ServerboundMidiInputPayload(
                synth.synthBlockEntity.getBlockPos(),
                time,
                synth.getPitchBend(),
                synth.getNetworkedNoteStack()
        );
    }

    @Override
    public @NonNull Type<ServerboundMidiInputPayload> type() {
        return ModPayloadTypes.SERVERBOUND_MIDI_INPUT;
    }

    public static void handle(ServerboundMidiInputPayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer controllingPlayer = context.player();
        ServerLevel level = controllingPlayer.level();
        if (!(level.getBlockEntity(payload.pos) instanceof SynthBlockEntity blockEntity))
            return;

        if (!(blockEntity.synth instanceof MidiInputSynth synth))
            return;

        if (!synth.updateMidiInputServer(controllingPlayer, payload.time, payload.noteStack))
            return;

        Vec3 pos = payload.pos.getCenter();
        ClientboundMidiInputPayload clientboundPayload = ClientboundMidiInputPayload.of(payload);
        level.players().forEach(player -> {
            if (player != controllingPlayer && player.distanceToSqr(pos) < BROADCAST_RANGE_SQR)
                ServerPlayNetworking.send(player, clientboundPayload);
        });
    }
}
