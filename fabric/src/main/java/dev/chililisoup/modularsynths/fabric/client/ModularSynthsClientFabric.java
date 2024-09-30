package dev.chililisoup.modularsynths.fabric.client;

import dev.chililisoup.modularsynths.client.ModularSynthsClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModularSynthsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModularSynthsClient.init();
    }
}
