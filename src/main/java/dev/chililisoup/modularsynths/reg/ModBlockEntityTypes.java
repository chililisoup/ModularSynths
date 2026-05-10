package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntityTypes {
    public static final BlockEntityType<SynthBlockEntity> SYNTH = register(
            "synth",
            SynthBlockEntity::new
    );

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<T> factory,
            Block... blocks
    ) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ModularSynths.id(name),
                FabricBlockEntityTypeBuilder.create(factory, blocks).build()
        );
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<T> factory
    ) {
        return register(name, factory, new Block[0]);
    }

    public static void init() {}
}
