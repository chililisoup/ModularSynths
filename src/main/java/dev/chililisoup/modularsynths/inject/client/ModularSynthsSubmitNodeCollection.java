package dev.chililisoup.modularsynths.inject.client;

import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface ModularSynthsSubmitNodeCollection {
    default List<CableSubmit> modularSynths$getCableSubmits() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    default List<CableSubmit> modularSynths$getTranslucentCableSubmits() {
        throw new UnsupportedOperationException("Implemented via Mixin.");
    }

    record CableSubmit(Matrix4f pose, CableFeatureRenderState state) {}
}
