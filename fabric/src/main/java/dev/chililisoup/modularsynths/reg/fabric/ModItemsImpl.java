package dev.chililisoup.modularsynths.reg.fabric;

import dev.chililisoup.modularsynths.reg.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;

import java.util.ArrayList;

public class ModItemsImpl {
    public static void registerDyeable(ArrayList<String> dyeable) {
        Item[] items = dyeable.stream().map(ModItems::get).toArray(Item[]::new);

        ColorProviderRegistry.ITEM.register(
                (itemStack, i) -> i > 0 ? -1 : ((DyeableLeatherItem)itemStack.getItem()).getColor(itemStack),
                items
        );
    }
}
