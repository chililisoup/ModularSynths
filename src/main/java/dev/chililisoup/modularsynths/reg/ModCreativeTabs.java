package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static final ResourceKey<CreativeModeTab> MAIN = register("main", () -> ModBlocks.SPEAKER.asItem().getDefaultInstance());

    private static ResourceKey<CreativeModeTab> register(String name, Supplier<ItemStack> iconSupplier) {
        ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                ModularSynths.id(name)
        );

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                tabKey,
                FabricCreativeModeTab.builder()
                        .icon(iconSupplier)
                        .title(Component.translatable("itemGroup.modularsynths." + name))
                        .build()
        );

        return tabKey;
    }

    public static void init() {}
}
