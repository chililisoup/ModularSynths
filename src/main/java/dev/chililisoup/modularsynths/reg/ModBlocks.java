package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.CableRelayBlock;
import dev.chililisoup.modularsynths.block.SinBlock;
import dev.chililisoup.modularsynths.block.SpeakerBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

@SuppressWarnings("unused")
public final class ModBlocks {
    public static final Block SPEAKER = registerSynth("speaker", SpeakerBlock::new);
    public static final Block CABLE_RELAY = registerSynth("cable_relay", CableRelayBlock::new);
    public static final Block SIN = registerSynth("sin", SinBlock::new);

    private static Block register(
            String name,
            Function<BlockBehaviour.Properties, Block> blockFactory,
            BlockBehaviour.Properties properties
    ) {
        Identifier id = ModularSynths.id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        Block block = blockFactory.apply(properties.setId(blockKey));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemProperties = new Item.Properties()
                .useBlockDescriptionPrefix()
                .requiredFeatures(block.requiredFeatures())
                .setId(itemKey);

        BlockItem blockItem = new BlockItem(block, itemProperties);
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.MAIN).register(tab -> tab.accept(blockItem));

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static Block registerSynth(
            String name,
            Function<BlockBehaviour.Properties, Block> blockFactory
    ) {
        Block block = register(name, blockFactory, BlockBehaviour.Properties.of());
        ModBlockEntityTypes.SYNTH.addValidBlock(block);
        return block;
    }

    public static void init() {}
}
