package io.github.anthonyclemens.WorldGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.GameObjects.Mobs.Fish;
import io.github.anthonyclemens.GameObjects.Mobs.Spider;
import io.github.anthonyclemens.GameObjects.Mobs.Zombie;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.BerryBush;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Grass;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.SingleTileObject;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Tree;

/**
 * Utility class for generating game objects for different biomes.
 */
public class GameObjectGenerator {

    static GameObject generateEnemyForBiome(Biome biome) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GameObjectGenerator(){};

    // Density values for object generation
    private static final double DESERT_CACTUS_DENSITY = 0.004;
    private static final double WATER_FISH_DENSITY = 0.001;
    private static final double PLAINS_TREE_DENSITY = 0.05;
    private static final double PLAINS_GRASS_DENSITY = 0.09;
    private static final double PLAINS_BERRY_DENSITY = 0.02;
    private static final double FOREST_TREE_DENSITY = 0.05;
    private static final double RAINFOREST_TREE_DENSITY = 0.08;
    private static final double TAIGA_TREE_DENSITY = 0.06;

    /**
     * Generates a list of GameObjects for a given biome and chunk.
     * @param biome     The biome type.
     * @param rand      Random instance.
     * @param chunkX    Chunk X coordinate.
     * @param chunkY    Chunk Y coordinate.
     * @param chunkSize Size of the chunk.
     * @return List of generated GameObjects.
     */
    public static List<GameObject> generateObjectsForBiome(Biome biome, Random rand, int chunkX, int chunkY, int chunkSize) {
        return switch (biome) {
            case DESERT -> generateDesertObjects(rand, chunkX, chunkY, chunkSize);
            case PLAINS -> generatePlainsObjects(rand, chunkX, chunkY, chunkSize);
            case WATER -> generateWaterObjects(rand, chunkX, chunkY, chunkSize);
            case FOREST -> generateForestObjects(rand, chunkX, chunkY, chunkSize);
            case RAINFOREST -> generateRainForestObjects(rand, chunkX, chunkY, chunkSize);
            case TAIGA -> generateTaigaObjects(rand, chunkX, chunkY, chunkSize);
            default -> new ArrayList<>();
        };
    }

    /**
     * Generates cactus objects for desert biomes.
     */
    private static List<GameObject> generateDesertObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();
        for (int y = 0; y < chunkSize - 1; y++) {
            for (int x = 0; x < chunkSize - 1; x++) {
                if (rand.nextFloat() < DESERT_CACTUS_DENSITY) {
                    SingleTileObject newObject = new SingleTileObject("main", "cactus", 9, x, y, chunkX, chunkY);
                    if (!isOverlapping(newObject, gobs)) {
                        gobs.add(newObject); // Add only if no overlap
                    }
                }
            }
        }
        return gobs;
    }

    /**
     * Generates fish objects for water biomes.
     */
    private static List<GameObject> generateWaterObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();
        int id = 0;
        for (int y = 0; y < chunkSize - 1; y++) {
            for (int x = 0; x < chunkSize - 1; x++) {
                if (rand.nextFloat() < WATER_FISH_DENSITY) {
                    Fish newObject = new Fish(x, y, chunkX, chunkY);
                    if (!isOverlapping(newObject, gobs)) {
                        id++;
                        gobs.add(newObject); // Add only if no overlap
                    }
                }
            }
        }
        return gobs;
    }

    /**
     * Generates tree objects for plains biomes.
     */
    private static List<GameObject> generatePlainsObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();
        int id = 0;
        for (int y = 0; y < chunkSize - 1; y++) {
            for (int x = 0; x < chunkSize - 1; x++) {
                if (rand.nextFloat() < PLAINS_TREE_DENSITY) {
                    Tree newObject = new Tree(rand, x, y, chunkX, chunkY);
                    if (!isOverlapping(newObject, gobs)) {
                        gobs.add(newObject);
                        id++;
                    }
                }
                if (rand.nextFloat() < PLAINS_GRASS_DENSITY) {
                    Grass newObject = new Grass(rand, x, y, chunkX, chunkY);
                    if (!isOverlapping(newObject, gobs)) {
                        gobs.add(newObject);
                        id++;
                    }
                }
                if(rand.nextFloat() < PLAINS_BERRY_DENSITY){
                    BerryBush newObject = new BerryBush(rand, x, y, chunkX, chunkY);
                    if (!isOverlapping(newObject, gobs)) {
                        gobs.add(newObject);
                        id++;
                    }
                }
            }
        }
        return gobs;
    }

    /**
     * Generates Dense tree objects for Forest biomes.
     */
    private static List<GameObject> generateForestObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();
        int id = 0;
        for (int y = 0; y < chunkSize - 1; y++) {
            for (int x = 0; x < chunkSize - 1; x++) {
                if (rand.nextFloat() < FOREST_TREE_DENSITY) {
                    Tree newObject = new Tree(rand, x, y, chunkX, chunkY, 1);
                    if (!isOverlapping(newObject, gobs)) {
                        gobs.add(newObject);
                        id++;
                    }
                }
            }
        }
        return gobs;
    }

    /**
     * Generates Dense tree objects for Forest biomes.
     */
    private static List<GameObject> generateRainForestObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();
        int id = 0;
        for (int y = 0; y < chunkSize - 1; y++) {
            for (int x = 0; x < chunkSize - 1; x++) {
                if (rand.nextFloat() < RAINFOREST_TREE_DENSITY) {
                    Tree newObject = new Tree(rand, x, y, chunkX, chunkY, 1);
                    if (!isOverlapping(newObject, gobs)) {
                        gobs.add(newObject);
                        id++;
                    }
                }
            }
        }
        return gobs;
    }

    /**
     * Generates Huge tree objects for Taiga biomes.
     */
    private static List<GameObject> generateTaigaObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();
        int id = 0;
        for (int y = 0; y < chunkSize - 1; y++) {
            for (int x = 0; x < chunkSize - 1; x++) {
                if (rand.nextFloat() < TAIGA_TREE_DENSITY) {
                    Tree newObject = new Tree(rand, x, y, chunkX, chunkY, true);
                    if (!isOverlapping(newObject, gobs)) {
                        gobs.add(newObject);
                        id++;
                    }
                }
            }
        }
        return gobs;
    }

    /**
     * Checks if a new object overlaps with any existing objects.
     */
    private static boolean isOverlapping(GameObject newObject, List<GameObject> existingObjects) {
        for (GameObject obj : existingObjects) {
            if (newObject.getHitbox().intersects(obj.getHitbox())) {
                return true; // Overlap detected
            }
        }
        return false; // No overlap
    }

    public static GameObject generateEnemyForBiome(Random rand, Biome biome, int chunkX, int chunkY) {
        return switch(biome){
            case PLAINS -> makeZombie(rand, chunkX, chunkY);
            case DESERT -> makeSpider(rand, chunkX, chunkY);
            default -> null;
        };
    }

    private static GameObject makeZombie(Random rand, int chunkX, int chunkY) {
        if(rand.nextFloat(1000) < 999.95) return null;
        int x = rand.nextInt(ChunkManager.CHUNK_SIZE);
        int y = rand.nextInt(ChunkManager.CHUNK_SIZE);
        return new Zombie(x, y, chunkX, chunkY);
    }

    private static GameObject makeSpider(Random rand, int chunkX, int chunkY) {
        if(rand.nextFloat(1000) < 999.95) return null;
        int x = rand.nextInt(ChunkManager.CHUNK_SIZE);
        int y = rand.nextInt(ChunkManager.CHUNK_SIZE);
        return new Spider(x, y, chunkX, chunkY);
    }
}
