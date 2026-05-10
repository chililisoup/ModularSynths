package dev.chililisoup.modularsynths.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderDispatcher.class)
public abstract class FeatureRenderDispatcherMixin {
    @Unique private final CableFeatureRenderer modularSynths$cableFeatureRenderer = new CableFeatureRenderer();

    @Shadow private @Final MultiBufferSource.BufferSource bufferSource;

    @Inject(method = "renderSolidFeatures", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/LeashFeatureRenderer;renderSolid(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
    ))
    private void renderModSolidFeatures(CallbackInfo ci, @Local(name = "collection") SubmitNodeCollection collection) {
        this.modularSynths$cableFeatureRenderer.renderSolid(collection, this.bufferSource);
    }

    @Inject(method = "renderTranslucentFeatures", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/CustomFeatureRenderer;renderTranslucent(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
    ))
    private void renderModTranslucentFeatures(CallbackInfo ci, @Local(name = "collection") SubmitNodeCollection collection) {
        this.modularSynths$cableFeatureRenderer.renderTranslucent(collection, this.bufferSource);
    }
}
