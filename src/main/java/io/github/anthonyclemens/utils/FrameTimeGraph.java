package io.github.anthonyclemens.utils;

public class FrameTimeGraph {
    private final float[] samples;
    private int index = 0;

    public FrameTimeGraph(int size) {
        samples = new float[size];
    }

    public void addSample(long nanos) {
        samples[index] = nanos / 1_000_000f;
        index = (index + 1) % samples.length;
    }

    public float[] getSamples() {
        return samples;
    }

    public int getIndex() {
        return index;
    }

}
