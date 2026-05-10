package dev.chililisoup.modularsynths.client.inject;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;

@SuppressWarnings("unused")
public interface ModularSynthsOrderedSubmitNodeCollector {
    default void modularSynths$submitCable(PoseStack poseStack, CableFeatureRenderState state) {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default void modularSynths$submitTranslucentCable(PoseStack poseStack, CableFeatureRenderState state) {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }
}
