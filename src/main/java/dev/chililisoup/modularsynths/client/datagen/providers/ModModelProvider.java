package dev.chililisoup.modularsynths.client.datagen.providers;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.state.ModBlockStateProperties;
import dev.chililisoup.modularsynths.reg.ModBlocks;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Optional;

import static dev.chililisoup.modularsynths.client.datagen.ModularSynthsDataGenerator.SYNTH_BLOCKS;

public final class ModModelProvider extends FabricModelProvider {
    public static final ModelTemplate SYNTH_MODEL = new ModelTemplate(
            Optional.of(ModularSynths.id("block/template_synth")), Optional.empty(), TextureSlot.TEXTURE
    );

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NonNull BlockModelGenerators generators) {
        generateSynthBlockStateModels(generators);
    }

    private static void generateSynthBlockStateModels(BlockModelGenerators generators) {
        ArrayList<Block> synthBlocks = new ArrayList<>(SYNTH_BLOCKS);

        createMidiInputBlock(generators, synthBlocks);

        synthBlocks.forEach(synthBlock -> generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(synthBlock, BlockModelGenerators.plainVariant(
                        SYNTH_MODEL.create(synthBlock, new TextureMapping().put(
                                TextureSlot.TEXTURE, TextureMapping.getBlockTexture(synthBlock)
                        ), generators.modelOutput)
                )).with(PropertyDispatch.modify(BlockStateProperties.ORIENTATION).generate(BlockModelGenerators::applyRotation))
        ));
    }

    private static void createMidiInputBlock(BlockModelGenerators generators, ArrayList<Block> blockList) {
        blockList.remove(ModBlocks.MIDI_INPUT);

        MultiVariant mono = BlockModelGenerators.plainVariant(
                SYNTH_MODEL.create(ModBlocks.MIDI_INPUT, new TextureMapping().put(
                        TextureSlot.TEXTURE, TextureMapping.getBlockTexture(ModBlocks.MIDI_INPUT)
                ), generators.modelOutput)
        );
        MultiVariant poly = BlockModelGenerators.plainVariant(
                SYNTH_MODEL.createWithSuffix(ModBlocks.MIDI_INPUT, "_poly", new TextureMapping().put(
                        TextureSlot.TEXTURE, TextureMapping.getBlockTexture(ModBlocks.MIDI_INPUT, "_poly")
                ), generators.modelOutput)
        );

        generators.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.MIDI_INPUT)
                .with(PropertyDispatch.initial(ModBlockStateProperties.POLYPHONIC)
                        .select(false, mono)
                        .select(true, poly)
                ).with(PropertyDispatch.modify(BlockStateProperties.ORIENTATION).generate(BlockModelGenerators::applyRotation))
        );
    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators generators) {}
}
