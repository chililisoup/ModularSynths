package dev.chililisoup.modularsynths.synthesis;

public record InputSampleSource(PolySampleSource[] inputSources) {
    public PolySampleSource get(int input, int size) {
        return this.inputSources.length > input && input >= 0 ?
                this.inputSources[input] :
                new PolySampleSource(new double[][]{ new double[size] });
    }

    public PolySampleSource get(int size) {
        return this.get(0, size);
    }
}
