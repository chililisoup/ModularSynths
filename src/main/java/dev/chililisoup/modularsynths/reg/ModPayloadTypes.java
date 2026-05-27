package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.network.ClientboundMidiInputPayload;
import dev.chililisoup.modularsynths.network.ClientboundOpenSynthScreenPayload;
import dev.chililisoup.modularsynths.network.ServerboundMidiInputPayload;
import dev.chililisoup.modularsynths.network.ServerboundSampleSynthUpdatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ModPayloadTypes {
    public static final CustomPacketPayload.Type<ServerboundSampleSynthUpdatePayload> SERVERBOUND_SAMPLE_SYNTH_UPDATE = registerServerboundPlay(
            "sample_synth_update",
            ServerboundSampleSynthUpdatePayload.STREAM_CODEC,
            ServerboundSampleSynthUpdatePayload::handle
    );

    public static final CustomPacketPayload.Type<ServerboundMidiInputPayload> SERVERBOUND_MIDI_INPUT = registerServerboundPlay(
            "midi_input",
            ServerboundMidiInputPayload.STREAM_CODEC,
            ServerboundMidiInputPayload::handle
    );

    public static final CustomPacketPayload.Type<ClientboundMidiInputPayload> CLIENTBOUND_MIDI_INPUT = registerClientboundPlay(
            "midi_input",
            ClientboundMidiInputPayload.STREAM_CODEC,
            ClientboundMidiInputPayload::handle
    );

    public static final CustomPacketPayload.Type<ClientboundOpenSynthScreenPayload> CLIENTBOUND_OPEN_SYNTH_SCREEN = registerClientboundPlay(
            "open_synth_screen",
            ClientboundOpenSynthScreenPayload.STREAM_CODEC,
            ClientboundOpenSynthScreenPayload::handle
    );

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> registerServerboundPlay(
            String name,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            ServerPlayNetworking.PlayPayloadHandler<T> handler
    ) {
        CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(ModularSynths.id(name));
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, handler);
        return type;
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> registerClientboundPlay(
            String name,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            ClientPlayNetworking.PlayPayloadHandler<T> handler
    ) {
        CustomPacketPayload.Type<T> type = new CustomPacketPayload.Type<>(ModularSynths.id(name));
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
        ClientPlayNetworking.registerGlobalReceiver(type, handler);
        return type;
    }

    public static void init() {}
}
