package dev.chililisoup.modularsynths.client.datagen.providers;

import dev.chililisoup.modularsynths.reg.ModCreativeTabs;
import dev.chililisoup.modularsynths.reg.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static dev.chililisoup.modularsynths.client.datagen.ModularSynthsDataGenerator.SYNTH_BLOCKS;

public final class ModEnglishLangProvider extends FabricLanguageProvider {
    public ModEnglishLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registryLookup, @NonNull TranslationBuilder translationBuilder) {
        translationBuilder.add(ModCreativeTabs.MAIN, "Modular Synths");
        translationBuilder.add(ModItems.PATCH_CABLE, "Patch Cable");

        SYNTH_BLOCKS.forEach(synthBlock -> translationBuilder.add(
                synthBlock,
                Arrays.stream(BuiltInRegistries.BLOCK.getKey(synthBlock).getPath().split("_"))
                        .map(StringUtils::capitalize)
                        .collect(Collectors.joining(" "))
        ));
    }
}
