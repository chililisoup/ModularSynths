package dev.chililisoup.modularsynths.synthesis;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;

public record SynthOutputConnection(BlockPos pos, int inPort) {
    public static final MapCodec<SynthOutputConnection> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(SynthOutputConnection::pos),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("inPort").forGetter(SynthOutputConnection::inPort)
    ).apply(i, SynthOutputConnection::new));

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof SynthOutputConnection(BlockPos otherPos, int otherInPort))) return false;
        if (!otherPos.equals(this.pos)) return false;
        return otherInPort == this.inPort;
    }
}
