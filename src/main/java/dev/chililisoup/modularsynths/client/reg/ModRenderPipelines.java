package dev.chililisoup.modularsynths.client.reg;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.chililisoup.modularsynths.ModularSynths;

import static net.minecraft.client.renderer.RenderPipelines.*;

public final class ModRenderPipelines {
    public static final RenderPipeline NO_DEPTH_LINES = register(
            RenderPipeline.builder(LINES_SNIPPET)
                    .withColorTargetState(ColorTargetState.DEFAULT)
                    .withLocation(ModularSynths.id("pipeline/no_depth_lines"))
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                    .build()
    );

    public static final RenderPipeline TRANSLUCENT_CABLE = register(
            RenderPipeline.builder(MATRICES_FOG_SNIPPET)
                    .withLocation(ModularSynths.id("pipeline/translucent_cable"))
                    .withVertexShader("core/rendertype_leash")
                    .withFragmentShader("core/rendertype_leash")
                    .withSampler("Sampler2")
                    .withCull(false)
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLE_STRIP)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .build()
    );

    public static void init() {}
}
