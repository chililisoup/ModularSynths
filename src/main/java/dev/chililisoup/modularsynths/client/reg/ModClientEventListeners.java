package dev.chililisoup.modularsynths.client.reg;

import dev.chililisoup.modularsynths.client.renderer.CablePreviewRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

@Environment(EnvType.CLIENT)
public final class ModClientEventListeners {
    static {
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(CablePreviewRenderer::submit);
    }

    public static void init() {}
}
