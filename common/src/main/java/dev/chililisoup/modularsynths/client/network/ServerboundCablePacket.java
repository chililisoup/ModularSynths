package dev.chililisoup.modularsynths.client.network;

import dev.chililisoup.modularsynths.ModularSynths;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ServerboundCablePacket {
    private final static ResourceLocation ID = new ResourceLocation(ModularSynths.MOD_ID, "serverbound_cable_packet");
    public static ResourceLocation id() {
        return ID;
    }

    public final BlockPos fromBlock;
    public final BlockPos toBlock;
    public final int fromIndex;
    public final int toIndex;

    private ServerboundCablePacket(BlockPos fromBlock, BlockPos toBlock, int fromIndex, int toIndex) {
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public static FriendlyByteBuf make(BlockPos fromBlock, BlockPos toBlock, int fromIndex, int toIndex) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(fromBlock);
        buf.writeBlockPos(toBlock);
        buf.writeInt(fromIndex);
        buf.writeInt(toIndex);
        return buf;
    }

    public static ServerboundCablePacket from(FriendlyByteBuf buffer) {
        BlockPos fromBlock = buffer.readBlockPos();
        BlockPos toBlock = buffer.readBlockPos();
        int fromIndex = buffer.readInt();
        int toIndex = buffer.readInt();
        return new ServerboundCablePacket(fromBlock, toBlock, fromIndex, toIndex);
    }
}
