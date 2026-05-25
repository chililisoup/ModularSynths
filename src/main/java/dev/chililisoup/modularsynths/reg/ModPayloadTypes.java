package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.network.ServerboundSampleSynthUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ModPayloadTypes {
    public static final CustomPacketPayload.Type<ServerboundSampleSynthUpdatePayload> SAMPLE_SYNTH_UPDATE = registerServerboundPlay(
            "sample_synth_update",
            ServerboundSampleSynthUpdatePayload.STREAM_CODEC,
            ServerboundSampleSynthUpdatePayload::handle
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

    public static void init() {}
}
