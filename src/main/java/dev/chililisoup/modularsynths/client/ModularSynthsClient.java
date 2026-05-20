package dev.chililisoup.modularsynths.client;

import dev.chililisoup.modularsynths.client.reg.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ModularSynthsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRenderPipelines.init();
        ModBlockEntityRenderers.init();
        SynthModuleRenderers.init();
        ModClientEventListeners.init();
    }
}
