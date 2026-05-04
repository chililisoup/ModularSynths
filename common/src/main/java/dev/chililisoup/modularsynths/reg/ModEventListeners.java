package dev.chililisoup.modularsynths.reg;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.chililisoup.modularsynths.block.SynthBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ModEventListeners {
    public static void init() {
        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            Level level = player.getCommandSenderWorld();
            if (!level.isClientSide) return EventResult.pass();

            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof SynthBlock block)
                return block.tryRemoveCables(state, level, pos, player)
                        ? EventResult.interruptFalse() : EventResult.pass();
            return EventResult.pass();
        });
    }
}
