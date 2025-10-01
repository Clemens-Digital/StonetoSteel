package io.github.anthonyclemens.GameObjects.Mobs;

import java.util.Random;

import io.github.anthonyclemens.GameObjects.SingleTileObjects.Item;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Items;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.WorldGen.Biome;
import io.github.anthonyclemens.WorldGen.Chunk;

public class Spider extends Mob{

    private final Item droppedItem;
    private boolean dropItem = false;

    public Spider(int x, int y, int chunkX, int chunkY) {
        super("spiders", x, y, chunkX, chunkY, "spider", 20);
        String spriteSheet = "spiders";
        this.animationIndex = (new Random().nextInt(2))*4;
        animationLoaders.put(Direction.UP, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex, 0, this.animationIndex, 3, 250));
        animationLoaders.put(Direction.DOWN, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+1, 0, this.animationIndex+1, 3, 250));
        animationLoaders.put(Direction.LEFT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+2, 0, this.animationIndex+2, 3, 250));
        animationLoaders.put(Direction.RIGHT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+3, 0, this.animationIndex+3, 3, 250));
        this.biomes = new Biome[]{Biome.DESERT, Biome.SWAMP, Biome.RAINFOREST, Biome.FOREST};
        this.droppedItem = new Item(Items.ITEM_STRING,x,y,chunkX,chunkY);
        this.droppedItem.setQuantity(new Random().nextInt(3)+1);
        this.intelligence = 0.8f;
        this.mobSpeed=3.5f;
        this.smoothness=0.01f;
        this.setSway(100f);
        this.maxHealth = 20;
        this.health = this.maxHealth;
        this.peaceful = false;
    }

    @Override
    public void update(IsoRenderer r, int deltaTime){
        if(this.dropItem){
            int[] currentLoc = r.screenToIsometric(renderX, renderY);
            Chunk thisChunk = r.getChunkManager().getChunk(chunkX, chunkY);
            this.droppedItem.setLocation(currentLoc[0],currentLoc[1],currentLoc[2],currentLoc[3]);
            thisChunk.addGameObject(this.droppedItem);
        }
        super.update(r,deltaTime);
        this.peaceful = r.isSunUp();
    }

    @Override
    public void removeHealth(int amount){
        super.removeHealth(amount);
        if (this.health <= 0) {
            this.dropItem = true;
        }
    }
}
