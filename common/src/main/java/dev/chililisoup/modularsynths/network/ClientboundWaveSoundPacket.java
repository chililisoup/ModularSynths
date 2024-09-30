package dev.chililisoup.modularsynths.network;

import dev.chililisoup.modularsynths.ModularSynths;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ClientboundWaveSoundPacket {
    private final static ResourceLocation ID =
            new ResourceLocation(ModularSynths.MOD_ID, "clientbound_wave_sound_packet");
    public static ResourceLocation id() {
        return ID;
    }

    public final BlockPos pos;

    private ClientboundWaveSoundPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static FriendlyByteBuf make(BlockPos pos) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        return buf;
    }

    public static ClientboundWaveSoundPacket from(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        return new ClientboundWaveSoundPacket(pos);
    }
}
