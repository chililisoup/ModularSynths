package dev.chililisoup.modularsynths.client.network;

import dev.architectury.networking.NetworkManager;
import dev.chililisoup.modularsynths.client.synthesis.SynthesizedAudioPlayer;
import dev.chililisoup.modularsynths.network.ClientboundWaveSoundPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;


@Environment(EnvType.CLIENT)
public class PacketHandlers {
    private static void registerReceiver(ResourceLocation id, NetworkManager.NetworkReceiver receiver) {
        NetworkManager.registerReceiver(NetworkManager.s2c(), id, receiver);
    }

    public static void registerReceivers() {
//        registerReceiver(ClientboundWaveSoundPacket.id(), (buf, context) -> {
//            ClientboundWaveSoundPacket packet = ClientboundWaveSoundPacket.from(buf);
//
//            short[] soundData = new short[44100];
//
//            for (int i = 0; i < soundData.length; i++) {
//                soundData[i] = (short) (32768 * Math.sin(Math.pow(0.001 * i, 2)));
//            }
//
//            SynthesizedAudioPlayer.playSound(
//                    packet.pos.getX(),
//                    packet.pos.getY(),
//                    packet.pos.getZ(),
//                    soundData
//            );
//        });
    }
}
