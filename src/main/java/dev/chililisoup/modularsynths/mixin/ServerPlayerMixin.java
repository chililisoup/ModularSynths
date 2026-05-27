package dev.chililisoup.modularsynths.mixin;

import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.inject.ModularSynthsPlayer;
import dev.chililisoup.modularsynths.network.ClientboundOpenSynthScreenPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ModularSynthsPlayer {
    @Override
    public void modularSynths$openSynthScreen(SynthBlockEntity synthBlockEntity) {
        ServerPlayNetworking.send(
                (ServerPlayer) (Object) this,
                new ClientboundOpenSynthScreenPayload(synthBlockEntity.getBlockPos())
        );
    }
}
