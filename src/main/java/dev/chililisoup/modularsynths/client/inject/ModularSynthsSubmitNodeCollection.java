package dev.chililisoup.modularsynths.client.inject;

import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;
import org.joml.Matrix4f;

import java.util.List;

public interface ModularSynthsSubmitNodeCollection {
    default List<CableSubmit> modularSynths$getCableSubmits() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default List<CableSubmit> modularSynths$getTranslucentCableSubmits() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    record CableSubmit(Matrix4f pose, CableFeatureRenderState state) {}
}
