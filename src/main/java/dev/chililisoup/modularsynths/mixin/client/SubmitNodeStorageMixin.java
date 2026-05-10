package dev.chililisoup.modularsynths.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SubmitNodeStorage.class)
public abstract class SubmitNodeStorageMixin implements SubmitNodeCollector {
    @Shadow public abstract @NonNull SubmitNodeCollection order(int order);

    @Override
    public void modularSynths$submitCable(PoseStack poseStack, CableFeatureRenderState state) {
        this.order(0).modularSynths$submitCable(poseStack, state);
    }

    @Override
    public void modularSynths$submitTranslucentCable(PoseStack poseStack, CableFeatureRenderState state) {
        this.order(0).modularSynths$submitTranslucentCable(poseStack, state);
    }
}
