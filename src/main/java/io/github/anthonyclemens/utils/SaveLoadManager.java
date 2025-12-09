package io.github.anthonyclemens.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.Achievements.Achievement;
import io.github.anthonyclemens.Logic.DayNightCycle;
import io.github.anthonyclemens.Player.Inventory;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.Camera;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.WorldGen.World;

public class SaveLoadManager {

    private DayNightCycle loadedEnv;
    private IsoRenderer loadedRenderer;
    private Camera loadedCamera;
    private float playerX;
    private float playerY;
    private float playerSpeed;
    private int playerHealth;
    private Inventory playerInventory;
    private List<Achievement> playerAchievements;

    public void saveGame(String folderPath, DayNightCycle env, World chunkManager, Camera camera, Player player) {
        Path saveRoot = Paths.get(folderPath);
        try {
            Files.createDirectories(saveRoot);
        } catch (IOException e) {
            Log.error("Failed to create save folder: " + e.getMessage());
            return;
        }
        Log.debug("Environment size: " + getSerializedSize(env));
        Log.debug("Camera size: " + getSerializedSize(camera));
        Log.debug("Player data size: " + getSerializedSize(new float[]{player.getX(), player.getY(), player.getSpeed()}));
        Log.debug("Player health size: " + getSerializedSize(player.getHealth()));
        Log.debug("Player Inventory size: " + getSerializedSize(player.getPlayerInventory()));
        Log.debug("Player Achievements size: " + getSerializedSize(player.getAchievementManager().getAllAchievements()));

        // Save individual components
        saveGzippedObject(saveRoot.resolve("environment.dat"), env);
        saveGzippedObject(saveRoot.resolve("camera.dat"), camera);
        saveGzippedObject(saveRoot.resolve("player.dat"), player.getX(), player.getY(), player.getSpeed(), player.getHealth(),player.getPlayerInventory(),player.getPlayerAchievements());
        saveGzippedObject(saveRoot.resolve("seed.dat"), chunkManager.getSeed());

        // Save chunks in 32x32 region groups
        saveChunkRegions(chunkManager, saveRoot.resolve("regions"));

        Log.debug("Save completed at " + folderPath + " total size: "+formatSize((int)getFolderSize(saveRoot.toFile())));
    }

    private void saveChunkRegions(World chunkManager, Path regionFolder) {
        try {
            Files.createDirectories(regionFolder);
        } catch (IOException e) {
            Log.error("Failed to create region folder: " + e.getMessage());
            return;
        }

        Map<String, List<Chunk>> groupedChunks = new HashMap<>();
        for (Chunk chunk : chunkManager.getChunks().values()) {
            if (!chunk.isDirty()) continue;
            int cx = chunk.getChunkX();
            int cy = chunk.getChunkY();
            String regionKey = (cx / 32) + "_" + (cy / 32);
            groupedChunks.computeIfAbsent(regionKey, k -> new ArrayList<>()).add(chunk);
        }

        for (Map.Entry<String, List<Chunk>> entry : groupedChunks.entrySet()) {
            String regionKey = entry.getKey();
            Path regionFile = regionFolder.resolve("region_" + regionKey + ".dat");
            saveGzippedObject(regionFile, entry.getValue());

            String label = "Chunks - region_" + regionKey;
            Log.debug(label + " total size: " + getSerializedSize(entry.getValue()));

            int tileBytes = 0;
            int gameObjectBytes = 0;

            for (Chunk c : entry.getValue()) {
                tileBytes += getSerializedSizeBytes(c.getTiles());
                gameObjectBytes += getSerializedSizeBytes(c.getGameObjects());
            }

            Log.debug(label + " tiles: " + formatSize(tileBytes));
            Log.debug(label + " gameObjects: " + formatSize(gameObjectBytes));
        }
    }

    private void saveGzippedObject(Path file, Object... objects) {
        try (FileOutputStream fos = new FileOutputStream(file.toFile());
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(gzos)) {

            for (Object obj : objects) {
                oos.writeObject(obj);
            }

        } catch (IOException e) {
            Log.error("Failed to save " + file.getFileName() + ": " + e.getMessage());
        }
    }

    public void loadGame(String folderPath, GameContainer container) {
        Path saveRoot = Paths.get(folderPath);

        try (ObjectInputStream envIn = openGzippedInput(saveRoot.resolve("environment.dat"));
             ObjectInputStream camIn = openGzippedInput(saveRoot.resolve("camera.dat"));
             ObjectInputStream playerIn = openGzippedInput(saveRoot.resolve("player.dat"));
             ObjectInputStream seedIn = openGzippedInput(saveRoot.resolve("seed.dat"))) {

            this.loadedEnv = (DayNightCycle) envIn.readObject();
            Log.debug("Loaded DayNightCycle");
            this.loadedCamera = (Camera) camIn.readObject();
            Log.debug("Loaded Camera");

            this.playerX = (float) playerIn.readObject();
            this.playerY = (float) playerIn.readObject();
            this.playerSpeed = (float) playerIn.readObject();
            this.playerHealth = (int) playerIn.readObject();
            this.playerInventory = (Inventory) playerIn.readObject();
            this.playerAchievements = (List<Achievement>) playerIn.readObject();
            Log.debug("Loaded Player");

            int seed = (int) seedIn.readObject();

            // Load chunks from dirty regions
            World cm = new World(seed);
            loadChunkRegions(cm, saveRoot.resolve("regions"));
            Log.debug("Loaded ChunkManager");

            this.loadedRenderer = new IsoRenderer(1f, "main", cm, container);
            Log.debug("Loaded IsoRenderer");

        } catch (IOException | ClassNotFoundException e) {
            Log.error("Failed to load game: " + e.getMessage());
        }
    }

    private void loadChunkRegions(World chunkManager, Path regionFolder) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(regionFolder, "*.dat")) {
            for (Path regionFile : files) {
                try (ObjectInputStream ois = openGzippedInput(regionFile)) {
                    List<Chunk> chunkList = (List<Chunk>) ois.readObject();
                    chunkManager.addDirtyChunks(chunkList);
                } catch (IOException | ClassNotFoundException e) {
                    Log.error("Failed to load region " + regionFile.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            Log.error("Region folder missing: " + e.getMessage());
        }
    }

    private ObjectInputStream openGzippedInput(Path file) throws IOException {
        return new ObjectInputStream(new GZIPInputStream(new FileInputStream(file.toFile())));
    }

    private String getSerializedSize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return formatSize(baos.size());
        } catch (IOException e) {
            Log.error("Size check failed: " + e.getMessage());
            return "null";
        }
    }

    private static String formatSize(int byteSize) {
        if (byteSize < 1024) return byteSize + " bytes";
        if (byteSize < 1024 * 1024) return String.format("%.2f KB", byteSize / 1024f);
        return String.format("%.2f MB", byteSize / (1024f * 1024f));
    }

    private int getSerializedSizeBytes(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return baos.size();
        } catch (IOException e) {
            Log.error("Size measurement failed: " + e.getMessage());
            return 0;
        }
    }

    public static boolean exists(String folderPath) {
        File saveFolder = new File(folderPath);
        return saveFolder.exists() && saveFolder.isDirectory();
    }

    public static void deleteSave(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists()) {
            deleteRecursive(folder);

            if (folder.delete()) {
                Log.debug("Save folder " + folderPath + " deleted.");
            } else {
                Log.error("Could not delete folder: " + folderPath);
            }
        } else {
            Log.error("Save folder not found: " + folderPath);
        }
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        if (!file.delete()) {
            Log.error("Failed to delete file or directory: " + file.getAbsolutePath());
        }
    }

    private static long getFolderSize(File folder) {
        long size = 0;
        File[] contents = folder.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (f.isFile()) {
                    size += f.length();
                } else {
                    size += getFolderSize(f);
                }
            }
        }
        return size;
    }

    public static String getSize(String folderPath) {
        File saveFolder = new File(folderPath);
        if (!saveFolder.exists() || !saveFolder.isDirectory()) {
            Log.warn("Save folder not found: " + folderPath);
            return "0";
        }

        long totalSize = 0;

        File[] files = saveFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    totalSize += file.length();
                } else if (file.isDirectory()) {
                    totalSize += getFolderSize(file);
                }
            }
        }

        return formatSize((int) totalSize);
    }

    public DayNightCycle getDayNightCycle() { return loadedEnv; }
    public IsoRenderer getRenderer() { return loadedRenderer; }
    public Camera getCamera() { return loadedCamera; }
    public float getPlayerX() { return playerX; }
    public float getPlayerY() { return playerY; }
    public float getPlayerSpeed() { return playerSpeed; }
    public int getPlayerHealth() { return playerHealth; }
    public Inventory getPlayerInventory() { return playerInventory; }
    public List<Achievement> getPlayerAchievements() { return playerAchievements; }
}