package dev.chililisoup.modularsynths.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chililisoup.modularsynths.inject.client.ModularSynthsSubmitNodeCollection;
import dev.chililisoup.modularsynths.client.renderer.feature.CableFeatureRenderState;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin implements OrderedSubmitNodeCollector, ModularSynthsSubmitNodeCollection {
    @Unique private final List<CableSubmit> modularSynths$cableSubmits = new ArrayList<>();
    @Unique private final List<CableSubmit> modularSynths$translucentCableSubmits = new ArrayList<>();

    @Shadow private boolean wasUsed;

    @Override
    public void modularSynths$submitCable(PoseStack poseStack, CableFeatureRenderState state) {
        this.wasUsed = true;
        this.modularSynths$cableSubmits.add(new CableSubmit(new Matrix4f(poseStack.last().pose()), state));
    }

    @Override
    public void modularSynths$submitTranslucentCable(PoseStack poseStack, CableFeatureRenderState state) {
        this.wasUsed = true;
        this.modularSynths$translucentCableSubmits.add(new CableSubmit(new Matrix4f(poseStack.last().pose()), state));
    }

    @Override
    public List<CableSubmit> modularSynths$getCableSubmits() {
        return this.modularSynths$cableSubmits;
    }

    @Override
    public List<CableSubmit> modularSynths$getTranslucentCableSubmits() {
        return this.modularSynths$translucentCableSubmits;
    }

    @Inject(method = "clear", at = @At("RETURN"))
    private void onReturnClear(CallbackInfo ci) {
        this.modularSynths$cableSubmits.clear();
        this.modularSynths$translucentCableSubmits.clear();
    }
}
