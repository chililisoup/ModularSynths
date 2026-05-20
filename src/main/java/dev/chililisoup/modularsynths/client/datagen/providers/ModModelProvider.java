package dev.chililisoup.modularsynths.client.datagen.providers;

import dev.chililisoup.modularsynths.ModularSynths;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

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
        SYNTH_BLOCKS.forEach(synthBlock -> {
            Material material = TextureMapping.getBlockTexture(synthBlock);
            TextureMapping mapping = new TextureMapping().put(TextureSlot.TEXTURE, material);
            generators.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(synthBlock, BlockModelGenerators.plainVariant(
                            SYNTH_MODEL.create(synthBlock, mapping, generators.modelOutput)
                    )).with(PropertyDispatch.modify(BlockStateProperties.ORIENTATION).generate(BlockModelGenerators::applyRotation))
            );
        });
    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators generators) {

    }
}
