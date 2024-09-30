package dev.chililisoup.modularsynths.client;

import dev.chililisoup.modularsynths.client.network.PacketHandlers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModularSynthsClient {
    public static void init() {
        PacketHandlers.registerReceivers();
    }
}
