package io.github.anthonyclemens.WorldGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.GameObjects.Mobs.Fish;
import io.github.anthonyclemens.GameObjects.Mobs.Spider;
import io.github.anthonyclemens.GameObjects.Mobs.Zombie;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.BerryBush;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Cactus;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Grass;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Tree;

/**
 * Utility class for generating game objects for different biomes.
 */
public class GameObjectGenerator {

    static GameObject generateEnemyForBiome(Biome biome) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GameObjectGenerator(){
        throw new UnsupportedOperationException("Utility class");
    }

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

        IntStream.range(0, chunkSize - 1).forEach(y ->
            IntStream.range(0, chunkSize - 1).forEach(x -> {
                Stream.of(
                    rand.nextFloat() < DESERT_CACTUS_DENSITY ? new Cactus(rand, x, y, chunkX, chunkY) : null
                )
                .filter(Objects::nonNull)
                .filter(obj -> !isOverlapping(obj, gobs))
                .forEach(gobs::add);
            })
        );

        return gobs;
    }

    /**
     * Generates fish objects for water biomes.
     */
    private static List<GameObject> generateWaterObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();

        IntStream.range(0, chunkSize - 1).forEach(y ->
            IntStream.range(0, chunkSize - 1).forEach(x -> {
                Stream.of(
                    rand.nextFloat() < WATER_FISH_DENSITY ? new Fish(x, y, chunkX, chunkY) : null
                )
                .filter(Objects::nonNull)
                .filter(obj -> !isOverlapping(obj, gobs))
                .forEach(gobs::add);
            })
        );

        return gobs;
    }

    /**
     * Generates tree objects for plains biomes.
     */
    private static List<GameObject> generatePlainsObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();

        IntStream.range(0, chunkSize - 1).forEach(y ->
            IntStream.range(0, chunkSize - 1).forEach(x -> {
                Stream.of(
                    rand.nextFloat() < PLAINS_TREE_DENSITY ? new Tree(rand, x, y, chunkX, chunkY) : null,
                    rand.nextFloat() < PLAINS_GRASS_DENSITY ? new Grass(rand, x, y, chunkX, chunkY) : null,
                    rand.nextFloat() < PLAINS_BERRY_DENSITY ? new BerryBush(rand, x, y, chunkX, chunkY) : null
                )
                .filter(Objects::nonNull)
                .filter(obj -> !isOverlapping(obj, gobs))
                .forEach(gobs::add);
            })
        );

        return gobs;
    }

    /**
     * Generates Dense tree objects for Forest biomes.
     */
    private static List<GameObject> generateForestObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();

        IntStream.range(0, chunkSize - 1).forEach(y ->
            IntStream.range(0, chunkSize - 1).forEach(x -> {
                Stream.of(
                    rand.nextFloat() < FOREST_TREE_DENSITY ? new Tree(rand, x, y, chunkX, chunkY, 1) : null
                )
                .filter(Objects::nonNull)
                .filter(obj -> !isOverlapping(obj, gobs))
                .forEach(gobs::add);
            })
        );

        return gobs;
    }

    /**
     * Generates Dense tree objects for Forest biomes.
     */
    private static List<GameObject> generateRainForestObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();

        IntStream.range(0, chunkSize - 1).forEach(y ->
            IntStream.range(0, chunkSize - 1).forEach(x -> {
                Stream.of(
                    rand.nextFloat() < RAINFOREST_TREE_DENSITY ? new Tree(rand, x, y, chunkX, chunkY, 1) : null
                )
                .filter(Objects::nonNull)
                .filter(obj -> !isOverlapping(obj, gobs))
                .forEach(gobs::add);
            })
        );

        return gobs;
    }

    /**
     * Generates Huge tree objects for Taiga biomes.
     */
    private static List<GameObject> generateTaigaObjects(Random rand, int chunkX, int chunkY, int chunkSize) {
        List<GameObject> gobs = new ArrayList<>();

        IntStream.range(0, chunkSize - 1).forEach(y ->
            IntStream.range(0, chunkSize - 1).forEach(x -> {
                Stream.of(
                    rand.nextFloat() < TAIGA_TREE_DENSITY ? new Tree(rand, x, y, chunkX, chunkY, true) : null
                )
                .filter(Objects::nonNull)
                .filter(obj -> !isOverlapping(obj, gobs))
                .forEach(gobs::add);
            })
        );

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
        if(rand.nextFloat(1000) < 999.96) return null;
        int x = rand.nextInt(World.CHUNK_SIZE);
        int y = rand.nextInt(World.CHUNK_SIZE);
        return new Zombie(x, y, chunkX, chunkY);
    }

    private static GameObject makeSpider(Random rand, int chunkX, int chunkY) {
        if(rand.nextFloat(1000) < 999.98) return null;
        int x = rand.nextInt(World.CHUNK_SIZE);
        int y = rand.nextInt(World.CHUNK_SIZE);
        return new Spider(x, y, chunkX, chunkY);
    }
}
