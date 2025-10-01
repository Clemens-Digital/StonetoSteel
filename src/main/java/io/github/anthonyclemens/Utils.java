package io.github.anthonyclemens;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.Log;

/**
 * Utility class providing helper methods for font loading and file path generation.
 */
public class Utils {

    /**
     * Generates a list of file paths with a numeric suffix and .ogg extension.
     * @param prefix Path prefix (e.g., "sounds/Player/Walk/Grass/walk").
     * @param start  Starting index (inclusive).
     * @param end    Ending index (exclusive).
     * @return List of file paths.
     */
    public static List<String> getFilePaths(String prefix, int start, int end){
        List<String> paths = new ArrayList<>();
        for(int i = start; i < end; i++){
            paths.add(prefix+String.format("%01d", i)+".ogg");
        }
        return paths;
    }

    public static void takeScreenShot(Graphics gfx, GameContainer gc) {
        int width = gc.getWidth();
        int height = gc.getHeight();
        BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        gfx.getArea(0,0, width, height, buffer);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte r = buffer.get();
                byte g = buffer.get();
                byte b = buffer.get();
                byte a = buffer.get();

                // Combine byte values into a single pixel integer
                int pixel = ((a & 0xFF) << 24) |
                        ((r & 0xFF) << 16) |
                        ((g & 0xFF) << 8) |
                        (b & 0xFF);

                // Flip the image vertically when setting the pixel
                screenshot.setRGB(x, height - y - 1, pixel);
            }
        }
        String filePath = "screenshot_" + System.currentTimeMillis() + ".png";
        if(!new File("screenshots").exists()){
            new File("screenshots").mkdirs();
        }
        try {
            File outputFile = new File("screenshots/"+filePath);
            ImageIO.write(screenshot, "png", outputFile);
            Log.debug("Image saved successfully to: screenshots/" + filePath);
        } catch (IOException e) {
            Log.error("Error saving image: " + e.getMessage());
        }
    }
}
