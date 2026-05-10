package dev.chililisoup.modularsynths.client.reg;

import dev.chililisoup.modularsynths.ModularSynths;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class ModRenderTypes {
    public static RenderType NO_DEPTH_LINES = RenderType.create(
            ModularSynths.id("no_depth_lines").toString(),
            RenderSetup.builder(ModRenderPipelines.NO_DEPTH_LINES)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup()
    );

    public static final RenderType TRANSLUCENT_CABLE = RenderType.create(
            ModularSynths.id("translucent_cable").toString(),
            RenderSetup.builder(ModRenderPipelines.TRANSLUCENT_CABLE)
                    .useLightmap()
                    .createRenderSetup()
    );
}
