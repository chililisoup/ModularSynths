package dev.chililisoup.modularsynths.synthesis;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.renderer.CableRenderState;
import dev.chililisoup.modularsynths.reg.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SynthInputConnection {
    public static final MapCodec<SynthInputConnection> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SynthInputConnection::pos),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("outPort").forGetter(SynthInputConnection::outPort),
            ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color").forGetter(SynthInputConnection::colorOptional)
    ).apply(i, SynthInputConnection::new));

    private final BlockPos pos;
    private final int outPort;
    private final int color;
    private @Nullable AbstractSynth synth = null;

    @Environment(EnvType.CLIENT) public @Nullable CableRenderState cableRenderState = null;

    private SynthInputConnection(BlockPos pos, int outPort, int color) {
        this.pos = pos;
        this.outPort = outPort;
        this.color = color;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private SynthInputConnection(BlockPos pos, int outPort, Optional<Integer> color) {
        this(pos, outPort, color.orElse(-1));
    }

    public SynthInputConnection(AbstractSynth synth, int outPort, int color) {
        this(synth.synthBlockEntity.getBlockPos(), outPort, color);
        this.synth = synth;
    }

    public BlockPos pos() {
        return this.pos;
    }

    public int outPort() {
        return this.outPort;
    }

    public int color() {
        return this.color;
    }

    public Optional<Integer> colorOptional() {
        return Optional.ofNullable(this.color == -1 ? null : this.color);
    }

    public @Nullable AbstractSynth synth() {
        return this.synth;
    }

    public void updateSynth(Level level) {
        if (level.getBlockEntity(this.pos) instanceof SynthBlockEntity synthBlockEntity)
            this.synth = synthBlockEntity.synth;
    }

    public ItemStack getItem() {
        ItemStack itemStack = new ItemStack(ModItems.PATCH_CABLE);
        this.colorOptional().ifPresent(color -> itemStack
                .set(DataComponents.DYED_COLOR, new DyedItemColor(color))
        );
        return itemStack;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SynthInputConnection otherConnection)) return false;
        if (!otherConnection.pos.equals(this.pos)) return false;
        return otherConnection.outPort == this.outPort;
    }
}
