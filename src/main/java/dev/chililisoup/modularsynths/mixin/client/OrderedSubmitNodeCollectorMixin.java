package dev.chililisoup.modularsynths.mixin.client;

import dev.chililisoup.modularsynths.client.inject.ModularSynthsOrderedSubmitNodeCollector;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OrderedSubmitNodeCollector.class)
public interface OrderedSubmitNodeCollectorMixin extends ModularSynthsOrderedSubmitNodeCollector {
}
