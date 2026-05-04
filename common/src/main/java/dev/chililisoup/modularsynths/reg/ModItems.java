package dev.chililisoup.modularsynths.reg;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.item.PatchCableItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ModItems {
    private static final Registrar<Item> ITEM_REGISTRAR = ModularSynths.MANAGER.get().get(Registries.ITEM);
    private static final HashMap<String, ModItem> ITEMS = new HashMap<>();

    private static void addItems() {
        new ModItem("patch_cable", PatchCableItem::new).dyeable().creativeTabs(ModCreativeTabs.MAIN.get());
    }

    public static void init() {
        addItems();
    }

    @Environment(EnvType.CLIENT)
    public static void processItems() {
        ArrayList<String> dyeable = new ArrayList<>();

        ITEMS.forEach((id, modItem) -> {
            addItem(modItem);
            if (modItem.dyeable) dyeable.add(modItem.id);
        });

        registerDyeable(dyeable);
    }

    private static void addItem(ModItem modItem) {
        ResourceLocation resourceLocation = new ResourceLocation(ModularSynths.MOD_ID, modItem.id);
        RegistrySupplier<? extends Item> item = ITEM_REGISTRAR.register(resourceLocation, () -> modItem.itemFactory.get(modItem.itemProperties));
        modItem.set(item);
    }

    public static Item get(String id) {
        return ITEMS.get(id).get();
    }

    private interface ItemFactory {
        Item get(Item.Properties props);
    }

    @ExpectPlatform
    private static void registerDyeable(ArrayList<String> dyeable) {
        throw new AssertionError();
    }

    private static final class ModItem {
        private final String id;
        private final ItemFactory itemFactory;
        private final Item.Properties itemProperties;
        private RegistrySupplier<? extends Item> item;

        boolean dyeable = false;

        ModItem(String id, ItemFactory itemFactory, Item.Properties itemProperties) {
            this.id = id;
            this.itemFactory = itemFactory;
            this.itemProperties = itemProperties;
            ITEMS.put(id, this);
        }

        ModItem(String id, ItemFactory itemFactory) {
            this(id, itemFactory, new Item.Properties());
        }

        void set(RegistrySupplier<? extends Item> item) {
            this.item = item;
        }

        Item get() {
            return item.get();
        }

        ModItem dyeable() {
            this.dyeable = true;
            return this;
        }

        ModItem creativeTabs(CreativeModeTab... tabs) {
            Arrays.stream(tabs).forEach(itemProperties::arch$tab);
            return this;
        }
    }
}
