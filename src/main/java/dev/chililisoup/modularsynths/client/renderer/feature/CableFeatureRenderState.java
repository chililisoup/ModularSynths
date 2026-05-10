package dev.chililisoup.modularsynths.client.renderer.feature;

import dev.chililisoup.modularsynths.client.renderer.CableRenderState;

public record CableFeatureRenderState(
        CableRenderState state,
        boolean selected,
        boolean selectionExists,
        int startBlockLight,
        int endBlockLight,
        int startSkyLight,
        int endSkyLight
) {}
