package dev.chililisoup.modularsynths.synthesis;

public record PolySampleSource(int id, double[]... polySamples) {
    private static int GLOBAL_ID = 0;

    public PolySampleSource(double[]... polySamples) {
        this(GLOBAL_ID++, polySamples);
    }

    public static PolySampleSource merged(int size, PolySampleSource... sources) {
        if (sources.length < 1) return new PolySampleSource(new double[size]);
        if (sources.length == 1) return sources[0];

        int channels = 0;
        for (PolySampleSource source : sources)
            if (source.channels() > channels)
                channels = source.channels();

        double[][] merged = new double[channels][];
        for (int channel = 0; channel < channels; channel++) {
            double[] mergedSamples = new double[size];
            for (PolySampleSource source : sources) {
                if (channel >= source.channels()) continue;
                double[] samples = source.polySamples[channel];
                for (int i = 0; i < size; i++) mergedSamples[i] += samples[i];
            }
            merged[channel] = mergedSamples;
        }

        return new PolySampleSource(merged);
    }

    public PolySampleSource copy() {
        return new PolySampleSource(this.safePolySamples());
    }

    public int channels() {
        return this.polySamples.length;
    }

    public double[][] safePolySamples() {
        double[][] polySamples = new double[this.polySamples.length][];
        for (int i = 0; i < polySamples().length; i++)
            polySamples[i] = this.polySamples[i].clone();
        return polySamples;
    }

    public double[] channelSamples(int channel, int size) {
        return this.channels() > channel && channel >= 0 ?
                this.polySamples[channel].clone() :
                new double[size];
    }

    public double[] monoSamples(int size) {
        if (this.channels() < 1) return new double[size];
        if (this.channels() == 1) return this.polySamples[0].clone();

        double[] monoSamples = new double[size];
        for (int i = 0; i < size; i++)
            for (double[] channel : this.polySamples)
                monoSamples[i] += channel[i] / this.channels();
        return monoSamples;
    }
}
