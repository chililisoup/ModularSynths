package dev.chililisoup.modularsynths.fabric.datagen;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.reg.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModularSynthsDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(ModelGenerator::new);
    }

    private static class ModelGenerator extends FabricModelProvider {
        private static final Block[] SYNTH_BLOCKS;

        private ModelGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
            ResourceLocation blankTexture = new ResourceLocation(ModularSynths.MOD_ID, "block/module_blank");

            for (Block synthBlock : SYNTH_BLOCKS) {
                TextureMapping synthTexture = new TextureMapping()
                        .put(TextureSlot.DOWN, blankTexture)
                        .put(TextureSlot.WEST, blankTexture)
                        .put(TextureSlot.EAST, blankTexture)
                        .put(TextureSlot.PARTICLE, blankTexture)
                        .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(synthBlock))
                        .put(TextureSlot.SOUTH, blankTexture)
                        .put(TextureSlot.UP, blankTexture);

                ResourceLocation synthModel = ModelTemplates.CUBE_DIRECTIONAL.create(
                        synthBlock,
                        synthTexture,
                        blockStateModelGenerator.modelOutput
                );

                blockStateModelGenerator.blockStateOutput.accept(
                        MultiVariantGenerator.multiVariant(synthBlock, Variant.variant().with(VariantProperties.MODEL, synthModel)
                        ).with(PropertyDispatch.property(BlockStateProperties.ORIENTATION)
                                .generate(fat -> blockStateModelGenerator.applyRotation(fat, Variant.variant()))
                        )
                );
            }
        }

        @Override
        public void generateItemModels(ItemModelGenerators itemModelGenerator) {}

        static {
            SYNTH_BLOCKS = ModBlocks.getAll().stream().filter(block -> block instanceof SynthBlock).toList().toArray(new Block[]{});
        }
    }
}
