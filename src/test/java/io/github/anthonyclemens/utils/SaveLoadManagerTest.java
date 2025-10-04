package io.github.anthonyclemens.utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.FastGraphics;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.WorldGen.World;

public class SaveLoadManagerTest {
    SaveLoadManager saveLoadManager;
    IsoRenderer preSaveRenderer;
    IsoRenderer postLoadRenderer;
    Player preSavePlayer;
    Player postLoadPlayer;

    @Before
    public void setup(){
        FastGraphics.setTestMode(true);
        saveLoadManager = new SaveLoadManager();
        World chunkManager = new World(123456);
        preSaveRenderer = new IsoRenderer(123, null, chunkManager, null);
        preSavePlayer = new Player(123, 123, 123);
        preSavePlayer.setVolume(0f);
        preSavePlayer.subtractHealth(99);
        saveLoadManager.saveGame("saves/testSave",null,chunkManager,null,preSavePlayer);

        saveLoadManager.loadGame("saves/testSave", null);
        postLoadRenderer = saveLoadManager.getRenderer();
        postLoadPlayer = new Player(saveLoadManager.getPlayerX(), saveLoadManager.getPlayerY(), saveLoadManager.getPlayerSpeed());
        postLoadPlayer.setHealth(saveLoadManager.getPlayerHealth());
    }

    @Test
    public void testChunkManagerSaveAndLoad(){
        Assert.assertSame("Biome for Chunk 0,0 should be the same after save and load.", preSaveRenderer.getChunkManager().getBiomeForChunk(0,0),postLoadRenderer.getChunkManager().getBiomeForChunk(0,0));
        Assert.assertEquals("In Chunk 0,0 Tile 0,0 should be the same after save and load.", preSaveRenderer.getChunkManager().getChunk(0, 0).getTile(0, 0),postLoadRenderer.getChunkManager().getChunk(0, 0).getTile(0, 0));
        Assert.assertEquals("Seed should be the same after save and load.",preSaveRenderer.getChunkManager().getSeed(), postLoadRenderer.getChunkManager().getSeed());
        Assert.assertEquals("GameObject id 0 in Chunk 0,0 should be the same after save and load.",preSaveRenderer.getChunkManager().getChunk(0,0).getGameObjects().get(0).getName(),
                postLoadRenderer.getChunkManager().getChunk(0,0).getGameObjects().get(0).getName());
    }

    @Test
    public void testPlayerData(){
        Assert.assertEquals("Player X location should be the same after save and load.",preSavePlayer.getX(), postLoadPlayer.getX(), 0);
        Assert.assertEquals("Player Y location should be the same after save and load.",preSavePlayer.getY(), postLoadPlayer.getY(), 0);
        Assert.assertEquals("Player speed should be the same after save and load.",preSavePlayer.getSpeed(), postLoadPlayer.getSpeed(), 0);
        Assert.assertEquals("Player health should be the same after save and load.",preSavePlayer.getHealth(), postLoadPlayer.getHealth(), 0);
    }

    @After
    public void cleanup() {
        SaveLoadManager.deleteSave("saves/testSave");
    }
}
