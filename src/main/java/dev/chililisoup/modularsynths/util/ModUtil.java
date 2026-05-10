package dev.chililisoup.modularsynths.util;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Optional;

public final class ModUtil {
    public static BlockHitResult getHitResult(Level level, Player player, Vec3 eyePos, float xRot, float yRot) {
        return level.clip(new ClipContext(
                eyePos,
                eyePos.add(player.calculateViewVector(xRot, yRot).scale(
                        eyePos.subtract(player.getEyePosition()).length()
                        + Optional.ofNullable(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE))
                                .map(AttributeInstance::getValue)
                                .orElse(20.0)
                )),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                CollisionContext.of(player)
        ));
    }

    public static BlockHitResult getHitResult(Level level, Player player, float partialTick) {
        Vec3 eyePos = player.getEyePosition(partialTick);

        return level.clip(new ClipContext(
                eyePos,
                eyePos.add(player.getViewVector(partialTick).scale(20)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
    }

    public static BlockHitResult getHitResult(Level level, Player player) {
        return getHitResult(level, player, 1);
    }
}
