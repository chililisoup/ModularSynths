package dev.chililisoup.modularsynths;

import dev.chililisoup.modularsynths.reg.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ModularSynths implements ModInitializer {
    public static final String MOD_ID = "modularsynths";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final AttachmentType<CableDraw> CABLE_DRAW = AttachmentRegistry.create(
            id("cable_draw"),
            builder -> builder.syncWith(CableDraw.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    );

    // grab from config
    public static final double SAMPLE_RATE = 44100; // 44.1 kHz
    public static final int SAMPLE_BUFFER_SIZE = 1024; // ~23.22ms delay (1000/(rate/buffer_size))
    public static final int GRAPHICS_RENDER_SCALE = 2; // Render work is divided by this amount (higher number is less quality)
    public static final int MAX_SEARCH_DEPTH = 50;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * Also returns true on singleplayer server thread
     */
    public static boolean isClientSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public void onInitialize() {
        ModBlockEntityTypes.init();
        ModCreativeTabs.init();
        ModBlocks.init();
        ModItems.init();
        ModEventListeners.init();
    }

    public record CableDraw(BlockPos pos, int port, boolean isInput, Vector3f portPos) {
        public static final StreamCodec<RegistryFriendlyByteBuf, CableDraw> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                CableDraw::pos,
                ByteBufCodecs.INT,
                CableDraw::port,
                ByteBufCodecs.BOOL,
                CableDraw::isInput,
                ByteBufCodecs.fromCodec(ExtraCodecs.VECTOR3F),
                CableDraw::portPos,
                CableDraw::new
        );

        public CableDraw(BlockPos pos, int port, boolean isInput, Vector3fc portPos) {
            this(pos, port, isInput, new Vector3f(portPos));
        }

        public CableDraw(BlockPos pos, int port, boolean isInput, Vec3 portPos) {
            this(pos, port, isInput, portPos.toVector3f());
        }
    }
}
