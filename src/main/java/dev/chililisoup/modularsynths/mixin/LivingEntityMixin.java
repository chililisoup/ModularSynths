package dev.chililisoup.modularsynths.mixin;

import dev.chililisoup.modularsynths.ModularSynths;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "stopUsingItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;causeUseVibration(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder$Reference;)V"
    ))
    private void clearCableDraw(CallbackInfo ci) {
        if (((LivingEntity) (Object) this) instanceof Player player)
            player.removeAttached(ModularSynths.CABLE_DRAW);
    }
}
