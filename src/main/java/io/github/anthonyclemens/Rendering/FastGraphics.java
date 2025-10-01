package io.github.anthonyclemens.Rendering;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;


public class FastGraphics extends Graphics{
    private Color lastColor = null;
    private static boolean testMode = false;

    public FastGraphics(){
        super();
        if (!testMode) {
            this.setFont(FontManager.getFont("Roboto", 14));
        }
    }

    @Override
    public void setColor(Color color) {
        if (!testMode && color != null && !color.equals(lastColor)) {
            super.setColor(color);
            lastColor = color;
        }
    }

    public static void setTestMode(boolean isTest) {
        testMode = isTest;
    }
}
