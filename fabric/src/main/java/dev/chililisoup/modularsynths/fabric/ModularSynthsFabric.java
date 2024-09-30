package dev.chililisoup.modularsynths.fabric;

import dev.chililisoup.modularsynths.ModularSynths;
import net.fabricmc.api.ModInitializer;

public class ModularSynthsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModularSynths.init();
    }
}