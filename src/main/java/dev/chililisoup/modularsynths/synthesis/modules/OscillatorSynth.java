package dev.chililisoup.modularsynths.synthesis.modules;

import com.mojang.serialization.Codec;
import dev.chililisoup.modularsynths.ModularSynths;
import dev.chililisoup.modularsynths.block.entity.SynthBlockEntity;
import dev.chililisoup.modularsynths.synthesis.AbstractSynth;
import dev.chililisoup.modularsynths.synthesis.InputSampleSource;
import dev.chililisoup.modularsynths.synthesis.PolySampleSource;
import dev.chililisoup.modularsynths.synthesis.SynthGraph;
import dev.chililisoup.modularsynths.util.SynthesisFunctions;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class OscillatorSynth extends AbstractSynth {
    private static final int WAVE_RESOLUTION = (int) (ModularSynths.SAMPLE_RATE / 35);
    private static final Codec<List<Float>> HARMONICS_CODEC = Codec.list(Codec.floatRange(0, 1), 16, 16);
    private float[] harmonics = new float[]{
            1.0F,
            0.9375F,
            0.875F,
            0.8125F,
            0.75F,
            0.6875F,
            0.625F,
            0.5625F,
            0.5F,
            0.4375F,
            0.375F,
            0.3125F,
            0.25F,
            0.1875F,
            0.125F,
            0.0625F
    };
    private boolean harmonicsChanged = true;
    private double[] wave = new double[]{0};
    private double @Nullable [] wavePreview = null;

    private Int2DoubleOpenHashMap phases = new Int2DoubleOpenHashMap();
    private Int2DoubleOpenHashMap previousPhases = new Int2DoubleOpenHashMap();

    public OscillatorSynth(SynthBlockEntity synthBlockEntity) {
        super(synthBlockEntity);
    }

    private void loadHarmonics(List<Float> harmonics) {
        float[] newHarmonics = new float[harmonics.size()];
        boolean match = true;
        for (int i = 0; i < this.harmonics.length; i++) {
            if (i >= newHarmonics.length) return;
            newHarmonics[i] = harmonics.get(i);
            if (this.harmonics[i] != newHarmonics[i]) match = false;
        }
        if (match) return;

        this.harmonics = newHarmonics;
        this.harmonicsChanged = true;
    }

    public void setHarmonic(int harmonic, float amplitude) {
        if (harmonic < 0 || harmonic >= this.harmonics.length) return;
        float clampedAmplitude = Math.clamp(amplitude, 0F, 1F);
        if (this.harmonics[harmonic] == clampedAmplitude) return;

        this.harmonics[harmonic] = clampedAmplitude;
        this.synthBlockEntity.setChanged();
    }

    public float[] getHarmonics() {
        return this.harmonics;
    }

    public List<Float> getHarmonicList() {
        return IntStream.range(0, this.harmonics.length).mapToObj(i -> this.harmonics[i]).toList();
    }

    public double @Nullable [] getWavePreview() {
        return this.wavePreview;
    }

    @Override
    public void load(ValueInput input) {
        super.load(input);
        input.read("harmonics", HARMONICS_CODEC).ifPresent(this::loadHarmonics);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.store("harmonics", HARMONICS_CODEC, this.getHarmonicList());
    }

    @Override
    public void afterLoad(Level level) {
        super.afterLoad(level);
        if (!this.harmonicsChanged) return;
        this.harmonicsChanged = false;
        if (level.isClientSide()) {
            this.wave = compileWave(this.harmonics);
            this.wavePreview = new double[128];
            for (int i = 0; i < this.wavePreview.length; i++)
                this.wavePreview[i] = this.processAt(i / (this.wavePreview.length - 1.0));
        }
    }

    private static double[] compileWave(float[] harmonics) {
        double[] wave = new double[WAVE_RESOLUTION];
        double max = 1.0;

        for (int h = 0; h < harmonics.length; h++) {
            if (harmonics[h] == 0F) continue;

            for (int i = 0; i < wave.length; i++) {
                double phase = i / (double) WAVE_RESOLUTION;
                wave[i] += SynthesisFunctions.sineWave(phase * (h + 1)) * harmonics[h];
                double abs = Math.abs(wave[i]);
                if (abs > max) max = abs;
            }
        }

        if (max > 1.0) for (int i = 0; i < wave.length; i++) {
            wave[i] /= max;
        }

        return wave;
    }

    public double processAt(double phase) {
        int floorIndex = Mth.floor(phase * this.wave.length) % this.wave.length;
        int ceilIndex = Mth.ceil(phase * this.wave.length) % this.wave.length;
        double mix = phase % 1.0;
        return this.wave[floorIndex] * (1.0 - mix) + this.wave[ceilIndex] * mix;
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
        PolySampleSource controlSource = inputs.get(1, size);
        if (controlSource.channels() == 0) return new PolySampleSource(new double[size]);
        PolySampleSource sampleSource = inputs.get(size);
        double[][] polySamples = new double[sampleSource.channels()][];

        for (int channel = 0; channel < sampleSource.channels(); channel++) {
            double[] control = controlSource.unsafeChannelSamples(channel, size);
            double[] samples = sampleSource.channelSamples(channel, size);

            double phase = this.previousPhases.get(channel);
            for (int i = 0; i < size; i++) {
                if (control[i] == 0.0) {
                    phase = 0.0;
                    continue;
                }
                double frequency = SynthesisFunctions.getFrequencyFromDouble(samples[i]);
                samples[i] = this.processAt(phase);
                phase += SynthesisFunctions.waveStep(frequency);
            }

            this.phases.put(channel, phase % 1.0);
            polySamples[channel] = samples;
        }

        return new PolySampleSource(polySamples);
    }

    @Override
    public Runnable bufferCleanupTask() {
        return this::updatePhases;
    }

    private void updatePhases() {
        this.previousPhases = this.phases;
        this.phases = new Int2DoubleOpenHashMap();
    }

    @Override
    public void powerOff() {
        this.phases.clear();
        this.previousPhases.clear();
    }
}
