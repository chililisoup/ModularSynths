package dev.chililisoup.modularsynths.client.network;

import dev.chililisoup.modularsynths.ModularSynths;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ServerboundCableRemovalPacket {
    private final static ResourceLocation ID = new ResourceLocation(ModularSynths.MOD_ID, "serverbound_cable_removal_packet");
    public static ResourceLocation id() {
        return ID;
    }

    public final BlockPos blockPos;
    public final int portIndex;
    public final boolean isInput;

    private ServerboundCableRemovalPacket(BlockPos blockPos, int portIndex, boolean isInput) {
        this.blockPos = blockPos;
        this.portIndex = portIndex;
        this.isInput = isInput;
    }

    public static FriendlyByteBuf make(BlockPos blockPos, int portIndex, boolean isInput) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(blockPos);
        buf.writeInt(portIndex);
        buf.writeBoolean(isInput);
        return buf;
    }

    public static ServerboundCableRemovalPacket from(FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        int portIndex = buffer.readInt();
        boolean isInput = buffer.readBoolean();
        return new ServerboundCableRemovalPacket(blockPos, portIndex, isInput);
    }
}
