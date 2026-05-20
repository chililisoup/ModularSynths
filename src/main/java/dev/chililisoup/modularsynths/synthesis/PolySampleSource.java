package dev.chililisoup.modularsynths.synthesis;

public record PolySampleSource(double[]... polySamples) {
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
                double[] samples = source.getFromChannel(channel, size);
                for (int i = 0; i < size; i++) mergedSamples[i] += samples[i];
            }
            merged[channel] = mergedSamples;
        }

        return new PolySampleSource(merged);
    }

    public int channels() {
        return this.polySamples.length;
    }

    public double[] getFromChannel(int channel, int size) {
        return this.channels() > channel && channel >= 0 ?
                this.polySamples[channel] :
                new double[size];
    }

    public double[] monoSamples(int size) {
        if (this.channels() < 1) return new double[size];
        if (this.channels() == 1) return this.polySamples[0];

        double[] monoSamples = new double[size];
        for (int i = 0; i < size; i++)
            for (double[] channel : this.polySamples)
                monoSamples[i] += channel[i] / this.channels();
        return monoSamples;
    }
}
