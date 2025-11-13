package io.github.anthonyclemens.Rendering;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

public class FontManager {
    private static final int MAX_CACHE_FONTS = 20;
    private FontManager() {}
    private static final HashMap<String, String> loadableFonts = new HashMap<>();
    private static final HashMap<String, TrueTypeFont> loadedFonts = new HashMap<>();

    public static void addFont(String name, String path){
        loadableFonts.put(name, path);
    }

    public static TrueTypeFont getFont(String name, int size){
        String found = (loadedFonts.get(name + "-" + size) != null) ? "found in cache." : "not in cache. Loading...";
        Log.debug("Requested font: " + name + "-" + size + " " + found);
        if(loadedFonts.get(name + "-" + size) == null){
            String path = loadableFonts.get(name);
            if(path == null){
                Log.error("Font not found: " + name);
                return null;
            }
            if(loadedFonts.size() >= MAX_CACHE_FONTS){
                loadedFonts.remove(loadedFonts.keySet().iterator().next());
                Log.debug("Font cache full. Removed oldest cached font.");
            }
            loadFont(name, path, size);
        }
        return loadedFonts.get(name+"-"+size);
    }

    private static void loadFont(String name, String path, int size){
        InputStream inputStream = null;
        try {
            inputStream = ResourceLoader.getResourceAsStream(path);
            // Create AWT Font from input stream
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            awtFont = awtFont.deriveFont((float)size);
            loadedFonts.put(name+"-"+size, new TrueTypeFont(awtFont, true));
            Log.debug(name + "-" + size + " cached.");
        } catch (FontFormatException | IOException e) {
            Log.error(e);
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) { /* ignore */ }
            }
        }
    }

}
