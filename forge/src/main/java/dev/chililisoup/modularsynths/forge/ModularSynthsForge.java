package dev.chililisoup.modularsynths.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.client.ModularSynthsClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ModularSynths.MOD_ID)
public class ModularSynthsForge {
    public ModularSynthsForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ModularSynths.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ModularSynths.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModularSynthsClient.init();
        }
    }
}