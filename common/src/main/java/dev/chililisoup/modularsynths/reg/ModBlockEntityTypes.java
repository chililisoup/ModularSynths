package dev.chililisoup.modularsynths.reg;

import dev.architectury.registry.registries.Registrar;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    private static final Registrar<BlockEntityType<?>> blockEntityTypes = ModularSynths.MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);

    public static Supplier<BlockEntityType<SynthBlockEntity>> SYNTH;

    public static void init() {
        SYNTH = addBlockEntityType("synth", SynthBlockEntity::new, ModBlocks.get("speaker"));
    }

    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> addBlockEntityType(
            String id,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Block ... validBlocks)
    {
        return blockEntityTypes.register(
                new ResourceLocation(ModularSynths.MOD_ID, id),
                () -> BlockEntityType.Builder.of(factory, validBlocks).build(null)
        );
    }
}
