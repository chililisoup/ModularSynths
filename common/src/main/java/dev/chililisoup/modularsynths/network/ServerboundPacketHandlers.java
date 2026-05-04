package dev.chililisoup.modularsynths.network;

import dev.architectury.networking.NetworkManager;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.network.ServerboundCablePacket;
import dev.chililisoup.modularsynths.client.network.ServerboundCableRemovalPacket;
import dev.chililisoup.modularsynths.item.PatchCableItem;
import dev.chililisoup.modularsynths.reg.ModBlockEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ServerboundPacketHandlers {
    private static void registerReceiver(ResourceLocation id, NetworkManager.NetworkReceiver receiver) {
        NetworkManager.registerReceiver(NetworkManager.c2s(), id, receiver);
    }

    public static void registerReceivers() {
        registerReceiver(ServerboundCablePacket.id(), ((buf, context) -> {
            ServerboundCablePacket packet = ServerboundCablePacket.from(buf);
            if (packet.fromIndex < 0 || packet.toIndex < 0) return;

            Player player = context.getPlayer();
            if (player.getServer() == null) return;

            player.getServer().execute(() -> {
                ItemStack stack = player.getMainHandItem();
                if (!(stack.getItem() instanceof PatchCableItem cableItem)) return;

                Level level = player.getCommandSenderWorld();

                BlockState fromBlock = level.getBlockState(packet.fromBlock);
                if (!(fromBlock.getBlock() instanceof SynthBlock fromSynth)) return;
                if (packet.fromIndex >= fromSynth.getOutputPositions().length) return;

                BlockState toBlock = level.getBlockState(packet.toBlock);
                if (!(toBlock.getBlock() instanceof SynthBlock toSynth)) return;
                if (packet.toIndex >= toSynth.getInputPositions().length) return;

                Optional<SynthBlockEntity> fromBlockEntity = level.getBlockEntity(packet.fromBlock, ModBlockEntityTypes.SYNTH.get());
                if (fromBlockEntity.isEmpty()) return;

                if (!fromBlockEntity.get().tryAddOutput(packet.toBlock, packet.fromIndex, packet.toIndex, cableItem.getColor(stack))) return;

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            });
        }));

        registerReceiver(ServerboundCableRemovalPacket.id(), ((buf, context) -> {
            ServerboundCableRemovalPacket packet = ServerboundCableRemovalPacket.from(buf);
            if (packet.portIndex < 0) return;

            Player player = context.getPlayer();
            if (player.getServer() == null) return;

            player.getServer().execute(() -> {
                Level level = player.getCommandSenderWorld();

                Optional<SynthBlockEntity> blockEntity = level.getBlockEntity(packet.blockPos, ModBlockEntityTypes.SYNTH.get());
                if (blockEntity.isEmpty()) return;

                if (packet.isInput) blockEntity.get().removeMatchingInputs(packet.blockPos, packet.portIndex);
                else blockEntity.get().removeMatchingOutputs(packet.blockPos, packet.portIndex);
            });
        }));
    }
}
