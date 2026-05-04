package dev.chililisoup.modularsynths.item;

import com.mojang.datafixers.util.Pair;
import dev.chililisoup.modularsynths.block.SynthBlock;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PatchCableItem extends Item implements DyeableLeatherItem {
    @Environment(EnvType.CLIENT) public SynthBlockEntity insertingBlockEntity;

    public PatchCableItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getColor(ItemStack stack) {
        CompoundTag compoundTag = stack.getTagElement("display");
        return compoundTag != null && compoundTag.contains("color", 99) ? compoundTag.getInt("color") : 0xffffff;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!level.isClientSide) return;
        if (this.insertingBlockEntity == null) return;
        if (!(livingEntity instanceof Player player)) return;

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        Optional<Pair<Boolean, Integer>> activePort = getActivePort(hitResult, level);

        if (activePort.isPresent()) {
            this.insertingBlockEntity.finishInsertCable(
                    hitResult.getBlockPos(),
                    activePort.get().getFirst(),
                    activePort.get().getSecond()
            );
        } else insertingBlockEntity.endInsertCable();

        this.insertingBlockEntity = null;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Environment(EnvType.CLIENT)
    private void beginDrawCable(Level level, BlockPos blockPos, boolean isInput, int portIndex, Vec3 drawStart) {
        if (level.getBlockEntity(blockPos) instanceof SynthBlockEntity synthBlockEntity) {
            synthBlockEntity.beginInsertCable(isInput, portIndex, drawStart);
            this.insertingBlockEntity = synthBlockEntity;
        }
    }

    private Optional<Pair<Boolean, Integer>> getActivePort(BlockHitResult hitResult, Level level) {
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return Optional.empty();

        BlockState blockState = level.getBlockState(hitResult.getBlockPos());
        if (!(blockState.getBlock() instanceof SynthBlock synthBlock))
            return Optional.empty();

        FrontAndTop fat = blockState.getValue(BlockStateProperties.ORIENTATION);
        if (hitResult.getDirection() != fat.front())
            return Optional.empty();

        Optional<Vec2> hitPos = SynthBlock.getHitPosition(hitResult, fat);
        if (hitPos.isEmpty())
            return Optional.empty();

        int inputPort = SynthBlock.hitPort(hitPos.get(), synthBlock.getInputPositions());
        if (inputPort >= 0)
            return Optional.of(new Pair<>(true, inputPort));

        int outputPort = SynthBlock.hitPort(hitPos.get(), synthBlock.getOutputPositions());
        if (outputPort >= 0)
            return Optional.of(new Pair<>(false, outputPort));

        return Optional.empty();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        if (!player.getAbilities().mayBuild) return InteractionResultHolder.fail(itemStack);

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        Optional<Pair<Boolean, Integer>> activePort = getActivePort(hitResult, level);
        if (activePort.isEmpty()) return InteractionResultHolder.fail(itemStack);

        if (level.isClientSide)
            beginDrawCable(level, hitResult.getBlockPos(), activePort.get().getFirst(), activePort.get().getSecond(), hitResult.getLocation());

        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(itemStack);
    }
}
