package dev.chililisoup.modularsynths.reg;

import dev.chililisoup.modularsynths.block.SynthBlock;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

public final class ModEventListeners {
    static {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            BlockState state = level.getBlockState(pos);
            return state.getBlock() instanceof SynthBlock<?> synthBlock
                    && synthBlock.attack(state, player, level, hand, pos, direction) ?
                    InteractionResult.SUCCESS : InteractionResult.PASS;
        });
    }

    public static void init() {}
}
