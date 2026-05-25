package dev.chililisoup.modularsynths.network;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.reg.ModPayloadTypes;
import dev.chililisoup.modularsynths.synthesis.modules.SamplerSynth;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

public record ServerboundSampleSynthUpdatePayload(BlockPos pos, Identifier sample) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSampleSynthUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerboundSampleSynthUpdatePayload::pos,
            Identifier.STREAM_CODEC, ServerboundSampleSynthUpdatePayload::sample,
            ServerboundSampleSynthUpdatePayload::new
    );

    @Override
    public @NonNull Type<ServerboundSampleSynthUpdatePayload> type() {
        return ModPayloadTypes.SAMPLE_SYNTH_UPDATE;
    }

    public static void handle(ServerboundSampleSynthUpdatePayload payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        ServerLevel level = player.level();
        BlockPos pos = payload.pos;

        if (!player.mayBuild() || !player.mayInteract(level, pos))
            return;

        if (!(level.getBlockEntity(pos) instanceof SynthBlockEntity blockEntity))
            return;

        if (blockEntity.synth instanceof SamplerSynth synth)
            synth.setSampleLocation(payload.sample);
    }
}
