package dev.chililisoup.modularsynths.client.datagen;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.AbstractSynthBlock;
import dev.chililisoup.modularsynths.client.datagen.providers.*;
import dev.chililisoup.modularsynths.reg.ModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class ModularSynthsDataGenerator implements DataGeneratorEntrypoint {
    public static final List<AbstractSynthBlock<?>> SYNTH_BLOCKS;

    @Override
    public void onInitializeDataGenerator(@NonNull FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModEnglishLangProvider::new);
    }

    static {
        SYNTH_BLOCKS = Arrays.stream(ModBlocks.class.getDeclaredFields()).map(field -> {
            try {
                if (field.get(null) instanceof AbstractSynthBlock<?> synthBlock)
                    return synthBlock;
            } catch (IllegalAccessException e) {
                ModularSynths.LOGGER.error(e);
            }
            return (AbstractSynthBlock<?>) null;
        }).filter(Objects::nonNull).toList();
    }
}
