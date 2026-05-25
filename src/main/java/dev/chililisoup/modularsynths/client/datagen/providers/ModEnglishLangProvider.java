package dev.chililisoup.modularsynths.client.datagen.providers;

import dev.chililisoup.modularsynths.reg.ModBlocks;
import dev.chililisoup.modularsynths.reg.ModCreativeTabs;
import dev.chililisoup.modularsynths.reg.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static dev.chililisoup.modularsynths.client.datagen.ModularSynthsDataGenerator.SYNTH_BLOCKS;

public final class ModEnglishLangProvider extends FabricLanguageProvider {
    public ModEnglishLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registryLookup, @NonNull TranslationBuilder builder) {
        builder.add("modularsynths.gui.sample_screen_edit_box", "Sample Location");

        String polyToMono = BuiltInRegistries.BLOCK.getKey(ModBlocks.POLY_TO_MONO).toLanguageKey();
        builder.add(polyToMono + ".sum", "SUM");
        builder.add(polyToMono + ".avg", "AVG");

        builder.add(ModCreativeTabs.MAIN, "Modular Synths");
        builder.add(ModItems.PATCH_CABLE, "Patch Cable");

        ArrayList<Block> synthBlocks = new ArrayList<>(SYNTH_BLOCKS);
        addAndRemove(builder, synthBlocks, ModBlocks.LFO, "LFO");
        addAndRemove(builder, synthBlocks, ModBlocks.MIDI_INPUT, "MIDI Input");

        synthBlocks.forEach(synthBlock -> builder.add(
                synthBlock,
                Arrays.stream(BuiltInRegistries.BLOCK.getKey(synthBlock).getPath().split("_"))
                        .map(StringUtils::capitalize)
                        .collect(Collectors.joining(" "))
        ));
    }

    private static void addAndRemove(
            TranslationBuilder translationBuilder, ArrayList<Block> blockList, Block block, String translation
    ) {
        blockList.remove(block);
        translationBuilder.add(block, translation);
    }
}
