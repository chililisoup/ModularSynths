package dev.chililisoup.modularsynths.reg;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.chililisoup.modularsynths.ModularSynths;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ModCreativeTabs {
    private static final Registrar<CreativeModeTab> creativeTabs = ModularSynths.MANAGER.get().get(Registries.CREATIVE_MODE_TAB);

    public static Supplier<CreativeModeTab> MAIN;

    public static void init() {
        MAIN = addCreativeTab("main", () -> ModBlocks.get("speaker").asItem());
    }

    public static Supplier<CreativeModeTab> addCreativeTab(String id, Supplier<Item> itemSupplier) {
        return creativeTabs.register(
                new ResourceLocation(ModularSynths.MOD_ID, id),
                () -> CreativeTabRegistry.create(
                        Component.translatable(String.format("category.%s.%s", ModularSynths.MOD_ID, id)),
                        () -> new ItemStack(itemSupplier.get())
                )
        );
    }
}
