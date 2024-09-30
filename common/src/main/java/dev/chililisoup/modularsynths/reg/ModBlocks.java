package dev.chililisoup.modularsynths.reg;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.ToneBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public class ModBlocks {
    private static final Registrar<Block> blocks = ModularSynths.MANAGER.get().get(Registries.BLOCK);
    private static final Registrar<Item> items = ModularSynths.MANAGER.get().get(Registries.ITEM);
    private static final HashMap<String, ModBlock> modBlocks = new HashMap<>();

    private static void addBlocks() {
        new ModBlock("tone_block", () -> new ToneBlock(BlockBehaviour.Properties.of())).creativeTabs(CreativeModeTabs.FUNCTIONAL_BLOCKS);
    }

    public static void init() {
        addBlocks();
        modBlocks.forEach((id, modBlock) -> addBlock(modBlock));
    }

    private static void addBlock(ModBlock modBlock) {
        ResourceLocation resourceLocation = new ResourceLocation(ModularSynths.MOD_ID, modBlock.id);
        RegistrySupplier<? extends Block> block = blocks.register(resourceLocation, modBlock.blockFactory);
        items.register(resourceLocation, () -> modBlock.getItem(block.get()));
        modBlock.set(block);
    }

    private interface ItemFactory {
        BlockItem get(Block block, Item.Properties props);
    }

    private static class ModBlock {
        private final String id;
        private final Supplier<? extends Block> blockFactory;
        private ItemFactory itemFactory = BlockItem::new;
        private final Item.Properties itemProperties = new Item.Properties();
        private RegistrySupplier<? extends Block> block;

        String renderType = null;

        ModBlock(String id, Supplier<? extends Block> blockFactory) {
            this.id = id;
            this.blockFactory = blockFactory;
            modBlocks.put(id, this);
        }

        ModBlock(String id, BlockBehaviour.Properties props) {
            this(id, () -> new Block(props));
        }

        final void set(RegistrySupplier<? extends Block> block) {
            this.block = block;
        }

        final Block get() {
            return block.get();
        }

        final BlockItem getItem(Block block) {
            return itemFactory.get(block, itemProperties);
        }

        final ModBlock itemFactory(ItemFactory factory) {
            itemFactory = factory;
            return this;
        }

        @SafeVarargs
        final ModBlock creativeTabs(ResourceKey<CreativeModeTab>... tabs) {
            Arrays.stream(tabs).forEach(tab -> itemProperties.arch$tab(tab));
            return this;
        }
    }
}
