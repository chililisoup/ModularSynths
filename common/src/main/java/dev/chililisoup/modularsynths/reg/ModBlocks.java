package dev.chililisoup.modularsynths.reg;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.*;
import dev.chililisoup.modularsynths.util.EffectType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
    private static final Registrar<Block> BLOCK_REGISTRAR = ModularSynths.MANAGER.get().get(Registries.BLOCK);
    private static final Registrar<Item> ITEM_REGISTRAR = ModularSynths.MANAGER.get().get(Registries.ITEM);
    private static final HashMap<String, ModBlock> BLOCKS = new HashMap<>();

    private static void addBlocks() {
//        new ModBlock("cable", () -> new CableBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("speaker", () -> new SpeakerBlockOld(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("sine_wave_module", () -> new WaveBlock(BlockBehaviour.Properties.of(), WaveType.SINE)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("square_wave_module", () -> new WaveBlock(BlockBehaviour.Properties.of(), WaveType.SQUARE)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("triangle_wave_module", () -> new WaveBlock(BlockBehaviour.Properties.of(), WaveType.TRIANGLE)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("sawtooth_wave_module", () -> new WaveBlock(BlockBehaviour.Properties.of(), WaveType.SAWTOOTH)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("noise_wave_module", () -> new WaveBlock(BlockBehaviour.Properties.of(), WaveType.NOISE)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("pitch_module", () -> new PitchBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("amp_effect_module", () -> new EffectBlock(BlockBehaviour.Properties.of(), EffectType.AMPLITUDE)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("portamento_effect_module", () -> new PortamentoBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("low_pass_filter_module", () -> new BasicFilterBlock(BlockBehaviour.Properties.of(), false)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("high_pass_filter_module", () -> new BasicFilterBlock(BlockBehaviour.Properties.of(), true)).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("lfo_module", () -> new LfoBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("dial", () -> new DialBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("envelope_module", () -> new EnvelopeBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("mono_midi_module", () -> new MonoMidiBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("poly_midi_module", () -> new PolyMidiBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("oscilloscope_module", () -> new OscilloscopeBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
//        new ModBlock("audio_visualizer_module", () -> new AudioVisualizerBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("synth_controller", () -> new SynthControllerBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("speaker", () -> new SpeakerBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("cable_relay", () -> new CableRelayBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("wave_source", () -> new WaveSourceBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("pitch_supplier", () -> new PitchSupplierBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("dial", () -> new DialBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("lfo", () -> new LfoBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("amplifier", () -> new EffectBlock(BlockBehaviour.Properties.of(), EffectType.AMPLITUDE)).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("inverter", () -> new EffectBlock(BlockBehaviour.Properties.of(), EffectType.INVERT)).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("pitch_shifter", () -> new PitchShifterBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("envelope", () -> new EnvelopeBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("portamento", () -> new PortamentoBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("mono_midi_input", () -> new MonoMidiBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("amplitude_display", () -> new AmplitudeDisplayBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("oscilloscope", () -> new OscilloscopeBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
        new ModBlock("circular_oscilloscope", () -> new CircularOscilloscopeBlock(BlockBehaviour.Properties.of())).creativeTabs(ModCreativeTabs.MAIN.get());
    }

    public static void init() {
        addBlocks();
        BLOCKS.forEach((id, modBlock) -> addBlock(modBlock));
    }

    private static void addBlock(ModBlock modBlock) {
        ResourceLocation resourceLocation = new ResourceLocation(ModularSynths.MOD_ID, modBlock.id);
        RegistrySupplier<? extends Block> block = BLOCK_REGISTRAR.register(resourceLocation, modBlock.blockFactory);
        ITEM_REGISTRAR.register(resourceLocation, () -> modBlock.getItem(block.get()));
        modBlock.set(block);
    }

    public static List<Block> getAll() {
        return BLOCKS.values().stream().map(ModBlock::get).toList();
    }

    public static Block get(String id) {
        return BLOCKS.get(id).get();
    }

    private interface ItemFactory {
        BlockItem get(Block block, Item.Properties props);
    }

    private static final class ModBlock {
        private final String id;
        private final Supplier<? extends Block> blockFactory;
        private ItemFactory itemFactory = BlockItem::new;
        private final Item.Properties itemProperties = new Item.Properties();
        private RegistrySupplier<? extends Block> block;

        String renderType = null;

        ModBlock(String id, Supplier<? extends Block> blockFactory) {
            this.id = id;
            this.blockFactory = blockFactory;
            BLOCKS.put(id, this);
        }

        ModBlock(String id, BlockBehaviour.Properties props) {
            this(id, () -> new Block(props));
        }

        void set(RegistrySupplier<? extends Block> block) {
            this.block = block;
        }

        Block get() {
            return block.get();
        }

        BlockItem getItem(Block block) {
            return itemFactory.get(block, itemProperties);
        }

        ModBlock itemFactory(ItemFactory factory) {
            itemFactory = factory;
            return this;
        }

        ModBlock creativeTabs(CreativeModeTab... tabs) {
            Arrays.stream(tabs).forEach(itemProperties::arch$tab);
            return this;
        }
    }
}
