package dev.chililisoup.modularsynths.inject.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public interface ModularSynthsOrderedSubmitNodeCollector {
    default void modularSynths$submitCable(PoseStack poseStack, CableFeatureRenderState state) {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default void modularSynths$submitTranslucentCable(PoseStack poseStack, CableFeatureRenderState state) {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }
}
