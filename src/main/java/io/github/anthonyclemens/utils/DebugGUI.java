package io.github.anthonyclemens.utils;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Sound.JukeBox;
import io.github.anthonyclemens.Sound.SoundBox;
import io.github.anthonyclemens.states.Game;
public class DebugGUI {
    /**
     * DebugGUI is a utility class for rendering debug information on the screen.
     * It provides methods to display FPS, memory usage, mouse position, selected tile and chunk,
     * zoom level, biome information, and sound status.
     */

    private String getMemUsage() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024L * 1024L) + "MB";
    }

    public void renderDebugGUI(Graphics g, GameContainer container, IsoRenderer renderer, Player player, float zoom, JukeBox jukeBox, SoundBox ambientSoundBox) {
        g.setColor(Color.black);
        int index = 0;

        int[] selectedBlock = player.getPlayerLocation();

        String tile = (selectedBlock.length >= 2) ? selectedBlock[0] + ", " + selectedBlock[1] : "N/A";
        String chunk = (selectedBlock.length >= 4) ? selectedBlock[2] + ", " + selectedBlock[3] : "N/A";
        String biome = "N/A";
        if (renderer != null &&  selectedBlock.length >= 4) {
            biome = String.valueOf(renderer.getChunkManager().getBiomeForChunk(selectedBlock[2], selectedBlock[3]));
        }
        String song = (jukeBox != null) ? jukeBox.getCurrentSong() : "N/A";
        String ambient = (ambientSoundBox != null) ? ambientSoundBox.getCurrentSound() : "N/A";
        String playerSound = player.getSound();
        String playerPos = player.getX()/18 + ", " + player.getY()/18;
        String playerHealth = String.valueOf(player.getHealth());
        String playerMaxHealth = String.valueOf(player.getMaxHealth());
        String seed = (renderer != null ? String.valueOf(renderer.getChunkManager().getSeed()) : "N/A");
        String paused = (Game.paused) ? "Yes" : "No";
        String chunkIsDirty = player.getCurrentChunk().isDirty() ? "Yes" : "No";
        String playerEquippedItem = (player.getEquippedItem() != null) ? player.getEquippedItem().name() : "None";

        String[] debugStrings = new String[] {
            "FPS: " + container.getFPS() + " FPS",
            "Memory Usage: " + getMemUsage(),
            "Tile: " + tile,
            "Chunk: " + chunk,
            "Chunk isDirty: " + chunkIsDirty,
            "Zoom level: " + Math.round(zoom * 100.0) / 100.0 + "x",
            "Biome: " + biome,
            "Song playing: " + song,
            "Ambient sound playing: " + ambient,
            "Player sound: " + playerSound,
            "Player position: " + playerPos,
            "Player health: " + playerHealth + "/" + playerMaxHealth,
            "Player equipped item: " + playerEquippedItem,
            "World Seed: " + seed,
            "Game Paused: " + paused,
        };

        for (String s : debugStrings) {
            g.drawString(s, 10, 20f + 20 * index);
            index++;
        }
    }
}
