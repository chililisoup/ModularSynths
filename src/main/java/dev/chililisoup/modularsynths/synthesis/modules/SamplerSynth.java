package dev.chililisoup.modularsynths.synthesis.modules;

import com.mojang.blaze3d.audio.Channel;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.client.ModClientUtil;
import dev.chililisoup.modularsynths.synthesis.*;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

public class SamplerSynth extends AbstractSynth {
    private static final Identifier DEFAULT_SAMPLE = SoundEvents.NOTE_BLOCK_HARP.value().location();

    private final Int2ObjectOpenHashMap<Status> channelStatuses = new Int2ObjectOpenHashMap<>();
    private boolean sampleChanged = true;
    private @NonNull Identifier sampleLocation = DEFAULT_SAMPLE;
    private double @Nullable [] sample = null;
    private int sampleBufferSize = 0;

    public SamplerSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    public void setSampleLocation(Identifier sampleLocation) {
        if (this.sampleLocation.equals(sampleLocation)) return;
        this.sampleLocation = sampleLocation;
        this.synthBlockEntity.setChanged();
    }

    public @NonNull Identifier getSampleLocation() {
        return this.sampleLocation;
    }

    @Override
    public void load(ValueInput input) {
        super.load(input);
        Identifier sampleLocation = input.read("sample", Identifier.CODEC).orElse(DEFAULT_SAMPLE);
        if (this.sampleLocation.equals(sampleLocation)) return;
        this.sampleLocation = sampleLocation;
        this.sampleChanged = true;
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.store("sample", Identifier.CODEC, this.sampleLocation);
    }

    @Override
    public void afterLoad(Level level) {
        super.afterLoad(level);
        if (!this.sampleChanged) return;
        this.sampleChanged = false;
        if (level.isClientSide()) this.loadSample();
    }

    @Override
    public int[] dependenciesFor(int outPort) {
        return new int[]{0, 1};
    }

    @Override
    public SynthGraph.NodeProcessor processorFor(int outPort) {
        return this::process;
    }

    private PolySampleSource process(InputSampleSource inputs, int size) {
        if (this.sample == null) return new PolySampleSource(new double[size]);

        double sampleBitSize = this.sampleBufferSize / ModularSynths.SAMPLE_RATE;

        double[][] polyFrequencies = inputs.get(size).polySamples();
        double[][] polyControl = inputs.get(1, size).polySamples();
        if (polyControl.length == 0) return new PolySampleSource(new double[size]);

        double[][] polyOutput = new double[polyFrequencies.length][size];
        for (int channel = 0; channel < polyOutput.length; channel++) {
            this.channelStatuses.put(channel, processChannel(
                    this.sample,
                    sampleBitSize,
                    polyFrequencies[channel],
                    channel < polyControl.length ?
                            polyControl[channel] : polyControl[polyControl.length - 1],
                    polyOutput[channel],
                    this.channelStatuses.getOrDefault(channel, Status.EMPTY)
            ));
        }

        return new PolySampleSource(polyOutput);
    }

    private static Status processChannel(
            double[] sample, double sampleBitSize, double[] frequencies, double[] control, double[] output, Status status
    ) {
        boolean triggered = status.triggered;
        double samplePosition = status.samplePosition;
        double frequency = status.frequency;
        double frequencyMultiplier = status.frequencyMultiplier;

        for (int i = 0; i < output.length; i++) {
            boolean nextTriggered = control[i] != 0.0;
            if (nextTriggered && !triggered) {
                samplePosition = 0.0;
            }
            triggered = nextTriggered;

            int sampleIndex = (int) samplePosition;
            if (sampleIndex >= sample.length) continue;

            if (frequency != frequencies[i]) {
                frequency = frequencies[i];
                double targetFrequency = SynthesisFunctions.getFrequencyFromDouble(frequency);
                frequencyMultiplier = targetFrequency / SynthesisFunctions.F_SHARP;
            }

            double floorSample = sample[sampleIndex];
            double mix = samplePosition % 1.0;

            if (mix == 0.0 || sampleIndex == sample.length - 1) {
                output[i] = floorSample;
                samplePosition += sampleBitSize * frequencyMultiplier;
                continue;
            }

            double ceilSample = sample[sampleIndex + 1];
            output[i] = floorSample * (1.0 - mix) + ceilSample * mix;

            samplePosition += sampleBitSize * frequencyMultiplier;
        }

        return new Status(triggered, samplePosition, frequency, frequencyMultiplier);
    }

    @Override
    public void powerOff() {
        this.channelStatuses.clear();
    }

    @Environment(EnvType.CLIENT)
    private void loadSample() {
        this.sample = null;
        this.sampleBufferSize = 0;
        this.channelStatuses.clear();

        SoundInstance soundInstance = ModClientUtil.getResolvedSoundInstance(this.sampleLocation);
        Sound sound = ModClientUtil.validateSoundInstance(soundInstance);
        if (sound == null) {
            ModularSynths.LOGGER.error("Sampler failed to find a valid sound for {}.", this.sampleLocation);
            return;
        }

        soundInstance.getAudioStream(ModClientUtil.soundManager().soundEngine.soundBuffers, sound.getPath(), false)
                .whenComplete((stream, err) -> this.loadSampleStream(sound.getLocation(), stream, err));
    }

    @Environment(EnvType.CLIENT)
    private void loadSampleStream(Identifier location, AudioStream stream, Throwable err) {
        if (err != null) {
            ModularSynths.LOGGER.error("Sampler failed to load sound {}.", location, err);
            return;
        }

        if (!(stream instanceof FiniteAudioStream finiteStream)) {
            ModularSynths.LOGGER.error("Sampler skipped loading sound {} as it is not finite.", location);
            return;
        }

        try {
            ByteBuffer buffer = finiteStream.readAll();
            double[] sample = new double[buffer.remaining() / 2];

            double denominator = Short.MAX_VALUE;
            for (int i = 0; i < sample.length; i++)
                sample[i] = buffer.getShort() / denominator;

            this.sampleBufferSize = Channel.calculateBufferSize(finiteStream.getFormat(), 1) / 2;
            this.sample = sample;

            if (ModularSynths.IS_DEV) ModularSynths.LOGGER.info(
                    "Sampler loaded sample {} with size {}/{}",
                    location,
                    sample.length,
                    this.sampleBufferSize
            );
        } catch (Exception e) {
            ModularSynths.LOGGER.error("Sampler failed to process sound {}.", location, e);
        }
    }

    private record Status(boolean triggered, double samplePosition, double frequency, double frequencyMultiplier) {
        private static final Status EMPTY = new Status(false, 0.0, 0.0, 0.0);
    }
}
