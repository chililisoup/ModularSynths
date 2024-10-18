package dev.chililisoup.modularsynths.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.client.ModularSynthsClient;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.function.Supplier;

@Mod(ModularSynths.MOD_ID)
public class ModularSynthsForge {
    private final IEventBus eventBus;

    private static final ArrayList<ModBlockRenderer<? extends BlockEntity>> BLOCK_ENTITY_RENDERERS = new ArrayList<>();

    public ModularSynthsForge(IEventBus eventBus) {
        this.eventBus = eventBus;

        EventBuses.registerModEventBus(ModularSynths.MOD_ID, eventBus);
        ModularSynths.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ModularSynthsClient.init();
            eventBus.addListener(this::registerEntityRenders);
        }
    }

    public static <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> blockEntityType, BlockEntityRendererProvider<T> blockEntityRenderer) {
        BLOCK_ENTITY_RENDERERS.add(new ModBlockRenderer<>(blockEntityType, blockEntityRenderer));
    }

    private <T extends BlockEntity> void registerEntityRender(EntityRenderersEvent.RegisterRenderers event, ModBlockRenderer<T> renderer) {
        event.registerBlockEntityRenderer(renderer.blockEntityType.get(), renderer.blockEntityRenderer);
    }

    public void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        BLOCK_ENTITY_RENDERERS.forEach(renderer -> registerEntityRender(event, renderer));
    }

    private record ModBlockRenderer<T extends BlockEntity>(
            Supplier<BlockEntityType<T>> blockEntityType,
            BlockEntityRendererProvider<T> blockEntityRenderer
    ) {}
}