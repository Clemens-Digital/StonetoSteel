package io.github.anthonyclemens.utils;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ProfilerRenderer {
    public static void renderGraph(Graphics g, FrameTimeGraph graph, float x, float y, float width, float height) {
        g.setColor(Color.black);
        float[] samples = graph.getSamples();
        int start = graph.getIndex();
        float barWidth = width / samples.length;

        // Compute stats
        float min = Float.MAX_VALUE, max = 0f, sum = 0f;
        for (float ms : samples) {
            if (ms <= 0f) continue;
            min = Math.min(min, ms);
            max = Math.max(max, ms);
            sum += ms;
        }
        float avg = sum / samples.length;

        // Draw bars
        for (int i = 0; i < samples.length; i++) {
            int sampleIndex = (start + i) % samples.length;
            float ms = samples[sampleIndex];

            float barHeight = Math.min(height, ms * (height / 33f));
            float ratio = Math.min(1f, ms / 33f);
            g.setColor(new Color(ratio, 1f - ratio, 0f));

            g.fillRect(x + i * barWidth, y + height - barHeight, barWidth, barHeight);
        }
        g.setColor(Color.black);
        // Reference lines (60 FPS = 16.66ms, 30 FPS = 33.33ms)
        float line144 = y + height - (6.94f * (height / 33f));
        float line60 = y + height - (16.66f * (height / 33f));
        float line30 = y + height - (33.33f * (height / 33f));

        g.drawLine(x, line144, x + width, line144);
        g.drawLine(x, line60, x + width, line60);
        g.drawLine(x, line30, x + width, line30);

        // Labels for lines
        g.drawString("120 FPS", x + width + 5, line144 - g.getFont().getLineHeight()/2);
        g.drawString("60 FPS", x + width + 5, line60 - g.getFont().getLineHeight()/2);
        g.drawString("30 FPS", x + width + 5, line30 - g.getFont().getLineHeight()/2);

        // Stats text (ms rounded)
        String stats = String.format("Min: %.1f ms   Avg: %.1f ms   Max: %.1f ms", min, avg, max);
        g.drawString(stats, x, y - g.getFont().getLineHeight() - 2);
        g.drawString("Frame Time", x + width / 2 - g.getFont().getWidth("Frame Time") / 2, y - (g.getFont().getLineHeight()*2) - 4);
        // Draw border
        g.drawRect(x - 1, y - 1, width + 2, height + 2);
    }
}