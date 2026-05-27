package dev.chililisoup.modularsynths.network;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.reg.ModPayloadTypes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public record ClientboundOpenSynthScreenPayload(BlockPos pos) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenSynthScreenPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundOpenSynthScreenPayload::pos,
            ClientboundOpenSynthScreenPayload::new
    );

    @Override
    public @NonNull Type<ClientboundOpenSynthScreenPayload> type() {
        return ModPayloadTypes.CLIENTBOUND_OPEN_SYNTH_SCREEN;
    }

    public static void handle(ClientboundOpenSynthScreenPayload payload, ClientPlayNetworking.Context context) {
        Level level = context.player().level();
        if (level.getBlockEntity(payload.pos) instanceof SynthBlockEntity blockEntity)
            context.player().modularSynths$openSynthScreen(blockEntity);
        else ModularSynths.LOGGER.warn(
                "Ignoring openSynthScreen on an invalid entity: {} at pos {}",
                level.getBlockEntity(payload.pos),
                payload.pos
        );
    }
}
