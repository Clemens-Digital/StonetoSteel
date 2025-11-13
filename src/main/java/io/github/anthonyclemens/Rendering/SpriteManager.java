package io.github.anthonyclemens.Rendering;

import java.util.HashMap;

import org.newdawn.slick.Animation;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.Log;

/**
 * Manages loading, retrieving, and removing SpriteSheets by name.
 * Provides utility methods for sprite dimensions.
 */
public class SpriteManager {
    public static boolean debugMode = false;
    private SpriteManager() {}
    private static final HashMap<String, SpriteSheet> spriteSheets = new HashMap<>();

    /**
     * Loads and adds a SpriteSheet to the manager.
     * @param name      Key name for the SpriteSheet.
     * @param path      Path to the sprite sheet image.
     * @param tileWidth Width of each tile.
     * @param tileHeight Height of each tile.
     */
    public static void addSpriteSheet(String name, String path, int tileWidth, int tileHeight) {
        try {
            SpriteSheet spriteSheet = new SpriteSheet(path, tileWidth, tileHeight);
            spriteSheets.put(name, spriteSheet);
        } catch (SlickException e) {
            if(debugMode) Log.error("Error loading sprite sheet: " + e.getMessage());
        }
    }

    /**
     * Removes a SpriteSheet and releases its resources.
     * @param name Key name of the SpriteSheet to remove.
     */
    public static void removeSpriteSheet(String name) {
        try {
            SpriteSheet sheet = spriteSheets.get(name);
            if (sheet != null) {
                sheet.destroy(); // Release native resources
            }
            spriteSheets.remove(name);
        } catch (Exception e) {
            if(debugMode) Log.error("Error removing sprite sheet: " + e.getMessage());
        }
    }

    /**
     * Retrieves a SpriteSheet by name.
     * @param name Key name of the SpriteSheet.
     * @return SpriteSheet instance or null if not found.
     */
    public static SpriteSheet getSpriteSheet(String name) {
        if(spriteSheets.get(name) == null){
            if(debugMode) Log.warn("Sprite sheet not found: " + name);
            return null;
        }
        return spriteSheets.get(name);
    }

    public static Animation getAnimation(String name, int sx, int sy, int ex, int ey, int frameDuration){
        SpriteSheet spriteSheet = getSpriteSheet(name);
        if (spriteSheet == null) {
            if(debugMode) Log.error("Cannot create animation, sprite sheet is null: " + name);
            return null;
        }
        return new Animation(spriteSheet, sx, sy, ex, ey, false, frameDuration, true);
    }

    /**
     * Gets the width of a single sprite in the sheet.
     * @param name Key name of the SpriteSheet.
     * @return Width of a sprite, or -1 if not found.
     */
    public static int getSpriteWidth(String name) {
        SpriteSheet spriteSheet = getSpriteSheet(name);
        if (spriteSheet == null) {
            if(debugMode) Log.error("Cannot get sprite width, sprite sheet is null: " + name);
            return -1;
        }
        return spriteSheet.getWidth()/spriteSheet.getHorizontalCount();
    }

    /**
     * Gets the height of a single sprite in the sheet.
     * @param name Key name of the SpriteSheet.
     * @return Height of a sprite, or -1 if not found.
     */
    public static int getSpriteHeight(String name) {
        SpriteSheet spriteSheet = getSpriteSheet(name);
        if (spriteSheet == null) {
            if(debugMode) Log.error("Cannot get sprite height, sprite sheet is null: " + name);
            return -1;
        }
        return spriteSheet.getHeight()/spriteSheet.getVerticalCount();
    }
}

