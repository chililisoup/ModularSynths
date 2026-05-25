package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.*;
import dev.chililisoup.modularsynths.synthesis.CableRelay;
import dev.chililisoup.modularsynths.synthesis.modules.*;
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
import net.minecraft.world.phys.Vec2;

import java.util.function.Function;

@SuppressWarnings("unused")
public final class ModBlocks {
    public static final Block SPEAKER = registerSynth("speaker", SpeakerBlock::new);
    public static final Block DIAL = registerSynth("dial", DialBlock::new);
    public static final Block NOTE_SUPPLIER = registerSynth("note_supplier", NoteSupplierBlock::new);
    public static final Block NOTE_SHIFTER = registerSynth("note_shifter", NoteShifterBlock::new);
    public static final Block MONITOR = registerSynth("monitor", MonitorBlock::new);
    public static final Block MIDI_INPUT = registerSynth("midi_input", MidiInputBlock::new);
    public static final Block POLY_TO_MONO = registerSynth("poly_to_mono", PolyToMonoBlock::new);
    public static final Block SAMPLER = registerSynth("sampler", SamplerSynthBlock::new);
    public static final Block WAVE_SOURCE = registerSynth("wave_source", properties -> new BasicSynthBlock<>(
            properties,
            WaveSourceSynth.class,
            WaveSourceSynth::new,
            new Vec2[]{new Vec2(13F / 16F, 8F / 16F)},
            new Vec2[]{
                    new Vec2(3F / 16F, 14F / 16F),
                    new Vec2(3F / 16F, 10F / 16F),
                    new Vec2(3F / 16F, 6F  / 16F),
                    new Vec2(3F / 16F, 2F  / 16F)
            }
    ));
    public static final Block CABLE_RELAY = registerSynth("cable_relay", properties -> new BasicSynthBlock<>(
            properties,
            CableRelay.class,
            CableRelay::new,
            new Vec2[]{
                    new Vec2(13F / 16F, 14F / 16F),
                    new Vec2(13F / 16F, 10F / 16F),
                    new Vec2(13F / 16F, 6F  / 16F),
                    new Vec2(13F / 16F, 2F  / 16F)
            },
            new Vec2[]{
                    new Vec2(3F / 16F, 14F / 16F),
                    new Vec2(3F / 16F, 10F / 16F),
                    new Vec2(3F / 16F, 6F  / 16F),
                    new Vec2(3F / 16F, 2F  / 16F)
            }
    ));
    public static final Block AMP = registerSynth("amp", properties -> new BasicSynthBlock<>(
            properties,
            AmpSynth.class,
            AmpSynth::new,
            new Vec2[]{
                    new Vec2(13F / 16F, 11F / 16F),
                    new Vec2(13F / 16F, 5F / 16F)
            },
            new Vec2[]{new Vec2(3F / 16F, 8F / 16F)}
    ));
    public static final Block INVERTER = registerSynth("inverter", properties -> new BasicSynthBlock<>(
            properties,
            InverterSynth.class,
            InverterSynth::new,
            new Vec2[]{
                    new Vec2(13F / 16F, 11F / 16F),
                    new Vec2(13F / 16F, 5F / 16F)
            },
            new Vec2[]{new Vec2(3F / 16F, 8F / 16F)}
    ));
    public static final Block LFO = registerSynth("lfo", properties -> new BasicSynthBlock<>(
            properties,
            LfoSynth.class,
            LfoSynth::new,
            new Vec2[]{new Vec2(13F / 16F, 8F / 16F)},
            new Vec2[]{new Vec2(3F / 16F, 8F / 16F)}
    ));
    public static final Block PORTAMENTO = registerSynth("portamento", properties -> new BasicSynthBlock<>(
            properties,
            PortamentoSynth.class,
            PortamentoSynth::new,
            new Vec2[]{
                    new Vec2(13F / 16F, 11F / 16F),
                    new Vec2(13F / 16F, 5F / 16F)
            },
            new Vec2[]{new Vec2(3F / 16F, 8F / 16F)}
    ));

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
