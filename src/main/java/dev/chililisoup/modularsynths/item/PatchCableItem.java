package dev.chililisoup.modularsynths.item;

import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.AbstractSynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.SynthInputConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class PatchCableItem extends Item {
    public PatchCableItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NonNull ItemStack itemStack, @NonNull LivingEntity user) {
        return 72000;
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(@NonNull ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, @NonNull LivingEntity entity, @NonNull ItemStack itemStack, int ticksRemaining) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;
        ModularSynths.CableDraw cableDraw = player.getAttached(ModularSynths.CABLE_DRAW);
        if (cableDraw == null) return;

        if (!(level.getBlockEntity(cableDraw.pos()) instanceof SynthBlockEntity)) {
            player.removeAttached(ModularSynths.CABLE_DRAW);
            player.stopUsingItem();
        }
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack stack, @NonNull Level level, @NonNull LivingEntity entity, int ticksRemaining) {
        if (!(entity instanceof Player player)) return false;
        ModularSynths.CableDraw fromDraw = player.getAttached(ModularSynths.CABLE_DRAW);
        if (fromDraw == null) return false;

        player.removeAttached(ModularSynths.CABLE_DRAW);
        if (level.isClientSide() || !player.mayBuild()) return false;

        if (!(level.getBlockEntity(fromDraw.pos()) instanceof SynthBlockEntity fromBlockEntity))
            return false;

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        ModularSynths.CableDraw toDraw = getCableDraw(hitResult, level);
        if (toDraw == null || toDraw.isInput() == fromDraw.isInput())
            return false;

        if (!(level.getBlockEntity(hitResult.getBlockPos()) instanceof SynthBlockEntity toBlockEntity))
            return false;

        int color = stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(-1)).rgb();
        return fromDraw.isInput() ?
                fromBlockEntity.synth.addInput(new SynthInputConnection(
                        toBlockEntity.synth, toDraw.port(), color
                ), fromDraw.port()) :
                toBlockEntity.synth.addInput(new SynthInputConnection(
                        fromBlockEntity.synth, fromDraw.port(), color
                ), toDraw.port());
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, Player player, @NonNull InteractionHand hand) {
        if (!player.mayBuild()) return InteractionResult.FAIL;

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        ModularSynths.CableDraw cableDraw = getCableDraw(hitResult, level);
        if (cableDraw == null) return InteractionResult.FAIL;

        if (!level.isClientSide()) {
            player.setAttached(ModularSynths.CABLE_DRAW, cableDraw);
            player.startUsingItem(hand);
        }

        return InteractionResult.CONSUME;
    }

    private static ModularSynths.@Nullable CableDraw getCableDraw(BlockHitResult hitResult, Level level) {
        if (hitResult.getType() != HitResult.Type.BLOCK) return null;

        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof AbstractSynthBlock<?> synthBlock)) return null;

        FrontAndTop orientation = blockState.getValue(AbstractSynthBlock.ORIENTATION);
        if (hitResult.getDirection() != orientation.front()) return null;

        Optional<AbstractSynthBlock.PortHit> portHit = AbstractSynthBlock.getPortHit(synthBlock, orientation, hitResult);
        return portHit.map(ModularSynths.CableDraw::new).orElse(null);
    }
}
