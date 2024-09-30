package dev.chililisoup.modularsynths.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class ClientboundCommunication {
    private static void broadcast(@Nullable Player except, ServerLevel level, double x, double y, double z, double radius, ResourceLocation id, FriendlyByteBuf buf) {
        PlayerList playerList = level.getServer().getPlayerList();

        for(int i = 0; i < playerList.getPlayerCount(); ++i) {
            ServerPlayer serverplayer = playerList.getPlayers().get(i);
            if (serverplayer != except && serverplayer.level().dimension() == level.dimension()) {
                double d0 = x - serverplayer.getX();
                double d1 = y - serverplayer.getY();
                double d2 = z - serverplayer.getZ();
                if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) {
                    NetworkManager.sendToPlayer(serverplayer, id, buf);
                }
            }
        }
    }

    public static void waveSound(Level level, BlockPos blockPos) {
        if (level instanceof ServerLevel)
            broadcast(
                    null,
                    (ServerLevel) level,
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ(),
                    16,
                    ClientboundWaveSoundPacket.id(),
                    ClientboundWaveSoundPacket.make(blockPos)
            );
    }
}
