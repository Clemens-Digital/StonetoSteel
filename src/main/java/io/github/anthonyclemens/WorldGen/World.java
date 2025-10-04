package io.github.anthonyclemens.WorldGen;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.GameObjects.GameObject;

/**
 * Manages world chunks, their generation, and biome assignment.
 * Handles chunk caching and provides utilities for chunk/block lookup.
 */
public class World{
    public static final int CHUNK_SIZE = 24;
    private final Map<String, Chunk> chunks = new ConcurrentHashMap<>();
    private final int seed;
    private PerlinNoise elevationGen;
    private PerlinNoise moistureGen;
    private PerlinNoise temperatureGen;
    // Biome generation tuning parameters
    private static final double ELEVATION_FREQ = 0.010;
    private static final double MOISTURE_FREQ = 0.028;
    private static final double TEMPERATURE_FREQ = 0.020;
    private static final double WATER_THRESHOLD = 0.3;
    private static final double BEACH_MIN_ELEV = WATER_THRESHOLD;
    private static final double BEACH_MAX_ELEV = 0.4;
    private static final int[][] NEIGHBOR_OFFSETS = {
        {-2, 0}, {2, 0}, {0, -2}, {0, 2},
        {-1, -1}, {-1, 1}, {1, -1}, {1, 1},
        {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    /**
     * Constructs a ChunkManager with the given world seed.
     * @param seed The world seed.
     */
    public World(int seed) {
        this.seed = seed;
        this.elevationGen = new PerlinNoise(seed);
        this.moistureGen = new PerlinNoise(seed + 1123);
        this.temperatureGen = new PerlinNoise(seed + 56424);
        Log.debug("ChunkManager initialized for infinite world generation with seed: " + seed);
    }

    /**
     * Gets the biome for a given chunk coordinate.
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     * @return The Biome for the chunk.
     */
    public Biome getBiomeForChunk(int chunkX, int chunkY) {

        double x = chunkX * ELEVATION_FREQ;
        double y = chunkY * ELEVATION_FREQ;

        double elevation   = normalize(elevationGen.generate(x, y));
        double moisture    = normalize(moistureGen.generate(chunkX * MOISTURE_FREQ, chunkY * MOISTURE_FREQ));
        double temperature = normalize(temperatureGen.generate(chunkX * TEMPERATURE_FREQ, chunkY * TEMPERATURE_FREQ));

        if (elevation < WATER_THRESHOLD) return Biome.WATER;

        boolean nearWater = false;
        for (int[] offset : NEIGHBOR_OFFSETS) {
            double nx = (chunkX + offset[0]) * ELEVATION_FREQ;
            double ny = (chunkY + offset[1]) * ELEVATION_FREQ;
            double neighborElev = normalize(elevationGen.generate(nx, ny));
            if (neighborElev < WATER_THRESHOLD) {
                nearWater = true;
                break;
            }
        }

        if (nearWater && elevation >= BEACH_MIN_ELEV && elevation <= BEACH_MAX_ELEV)
            return Biome.BEACH;

        return Biome.getBiomeFromClimate(elevation, moisture, temperature);
    }

    /**
     *
     * @param val
     * @return Brings -1.0..1.0 to 0.0..1.0
     */

    private double normalize(double val) {
        return (val + 1.0) / 2.0;
    }

    private void createPerlin(){
        this.elevationGen = new PerlinNoise(seed);
        this.moistureGen = new PerlinNoise(seed + 1000);
        this.temperatureGen = new PerlinNoise(seed + 5000);
    }

    /**
     * Gets or generates a chunk at the specified coordinates.
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     * @return The Chunk instance.
     */
    public Chunk getChunk(int chunkX, int chunkY) {
        if(elevationGen==null) createPerlin();
        String key = chunkX + "," + chunkY;
        return chunks.computeIfAbsent(key, k -> {
            Biome biome = getBiomeForChunk(chunkX, chunkY);
            Biome northBiome = getBiomeForChunk(chunkX, chunkY - 1);
            Biome southBiome = getBiomeForChunk(chunkX, chunkY + 1);
            Biome westBiome = getBiomeForChunk(chunkX - 1, chunkY);
            Biome eastBiome = getBiomeForChunk(chunkX + 1, chunkY);
            return new Chunk(CHUNK_SIZE, biome, chunkX, chunkY, seed + chunks.size(),
                            new Biome[] {northBiome, southBiome, westBiome, eastBiome});
        });
    }

    /**
     * Converts absolute block coordinates to chunk and block indices.
     * @param absX Absolute X coordinate.
     * @param absY Absolute Y coordinate.
     * @return Array: [blockX, blockY, chunkX, chunkY]
     */
    public int[] getBlockAndChunk(int absX, int absY) {
        int chunkX = (absX < 0) ? (absX + 1) / CHUNK_SIZE - 1 : absX / CHUNK_SIZE;
        int chunkY = (absY < 0) ? (absY + 1) / CHUNK_SIZE - 1 : absY / CHUNK_SIZE;

        int tileX = (absX % CHUNK_SIZE + CHUNK_SIZE) % CHUNK_SIZE;
        int tileY = (absY % CHUNK_SIZE + CHUNK_SIZE) % CHUNK_SIZE;

        return new int[]{tileX, tileY, chunkX, chunkY};
    }

    /**
     * Adds a GameObject to the appropriate chunk.
     * @param obj The GameObject to add.
     */
    public void addGameObject(GameObject obj) {
        this.getChunk(obj.getCX(), obj.getCY()).addGameObject(obj);
    }

    /**
     * Adds multiple GameObjects to a specific chunk.
     * @param gobs   List of GameObjects.
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     */
    public void addGameObjects(List<GameObject> gobs, int chunkX, int chunkY) {
        this.getChunk(chunkX, chunkY).addGameObjects(gobs);
    }

    /**
     * Removes a GameObject by index from a specific chunk.
     * @param idx    Index of the GameObject.
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     */
    public void removeGameObject(UUID uuid, int chunkX, int chunkY) {
        this.getChunk(chunkX, chunkY).removeGameObject(uuid);
    }

    public int getSeed() {
        return seed;
    }

    public Map<String, Chunk> getChunks() {
        return Collections.unmodifiableMap(chunks);
    }

    public void addChunk(Chunk chunk) {
        String key = chunk.getChunkX() + "," + chunk.getChunkY();
        chunks.put(key, chunk);
    }

    public void moveGameObjectToChunk(GameObject obj, int oldChunkX, int oldChunkY, int newChunkX, int newChunkY) {
        Chunk oldChunk = getChunk(oldChunkX, oldChunkY);
        if (obj != null && oldChunk.getGameObjects().contains(obj)) {
            oldChunk.removeGameObject(obj.getUUID());
            Chunk newChunk = getChunk(newChunkX, newChunkY);
            newChunk.addGameObject(obj);
            newChunk.setDirty(true);
        } else {
            Log.warn("GameObject not found in chunk (" + oldChunkX + ", " + oldChunkY + ") - obj uuid: " + (obj!=null?obj.getUUID(): "null"));
        }
    }

    public List<Chunk> getDirtyChunks() {
        return chunks.values().parallelStream().filter(Chunk::isDirty).toList();
    }

    public void addDirtyChunks(List<Chunk> dirtyChunks) {
        for (Chunk chunk : dirtyChunks) {
            String key = chunk.getChunkX() + "," + chunk.getChunkY();
            chunks.put(key, chunk);
        }
    }

}
