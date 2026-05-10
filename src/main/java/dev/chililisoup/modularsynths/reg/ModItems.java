package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.item.PatchCableItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {
    public static final Item PATCH_CABLE = register("patch_cable", PatchCableItem::new);

    private static Item register(
            String name,
            Function<Item.Properties, Item> itemFactory,
            Item.Properties properties
    ) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ModularSynths.id(name));
        Item item = itemFactory.apply(properties.setId(itemKey));
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> tab.accept(item));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    private static Item register(
            String name,
            Function<Item.Properties, Item> itemFactory
    ) {
        return register(name, itemFactory, new Item.Properties());
    }

    public static void init() {}
}
