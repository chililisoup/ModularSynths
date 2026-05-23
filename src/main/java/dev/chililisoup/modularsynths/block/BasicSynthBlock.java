package dev.chililisoup.modularsynths.block;

import com.mojang.serialization.MapCodec;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public class BasicSynthBlock<T extends AbstractSynth> extends AbstractSynthBlock<T> {
    private final MapCodec<BasicSynthBlock<T>> codec;
    private final Vec2[] inputPositions;
    private final Vec2[] outputPositions;
    private final Function<SynthBlockEntity, T> synthFactory;

    @Override
    protected @NonNull MapCodec<? extends AbstractSynthBlock<T>> codec() {
        return this.codec;
    }

    public BasicSynthBlock(
            Properties properties,
            Class<T> synthClass,
            Function<SynthBlockEntity, T> synthFactory,
            Vec2[] inputPositions,
            Vec2[] outputPositions
    ) {
        super(properties, synthClass);
        this.synthFactory = synthFactory;
        this.inputPositions = inputPositions;
        this.outputPositions = outputPositions;

        this.codec = simpleCodec(props -> new BasicSynthBlock<>(
                props, this.synthClass, this.synthFactory, this.inputPositions, this.outputPositions
        ));
    }

    public BasicSynthBlock(Properties properties, Class<T> synthClass, Function<SynthBlockEntity, T> synthFactory, Vec2[] outputPositions) {
        this(properties, synthClass, synthFactory, new Vec2[0], outputPositions);
    }

    public BasicSynthBlock(Properties properties, Class<T> synthClass, Function<SynthBlockEntity, T> synthFactory) {
        this(properties, synthClass, synthFactory, new Vec2[0]);
    }

    @Override
    public Vec2[] inputPositions() {
        return this.inputPositions;
    }

    @Override
    public Vec2[] outputPositions() {
        return this.outputPositions;
    }

    @Override
    public @NonNull T newSynth(SynthBlockEntity synthBlockEntity) {
        return this.synthFactory.apply(synthBlockEntity);
    }
}
