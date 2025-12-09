package io.github.anthonyclemens.GameObjects.Mobs;

import java.util.List;
import java.util.Random;

import io.github.anthonyclemens.GameObjects.Items;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Item;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.WorldGen.Biome;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.states.Game;

public class Fish extends Mob{
    private final Item droppedItem;
    private boolean dropItem = false;

    public Fish(int x, int y, int chunkX, int chunkY) {
        super("fishes", x, y, chunkX, chunkY, "fish", 20);
        String spriteSheet = "fishes";
        this.animationIndex = new Random().nextInt(3)*2;
        animationLoaders.put(Direction.UP, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex, 0, this.animationIndex, 0, 100));
        animationLoaders.put(Direction.DOWN, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex, 0, this.animationIndex, 0, 100));
        animationLoaders.put(Direction.LEFT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+1, 0, this.animationIndex+1, 0, 100));
        animationLoaders.put(Direction.RIGHT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex, 0, this.animationIndex, 0, 100));
        this.droppedItem = new Item(spriteSheet, "ITEM_FISH", this.animationIndex, x, y, chunkX, chunkY);
        this.droppedItem.setQuantity(1);
        this.biomes = new Biome[]{Biome.WATER};
        this.mobSpeed=1f;
        this.smoothness=0.02f;
        this.setSway(1000f);
        this.maxHealth = 10;
        this.health = this.maxHealth;
    }

    @Override
    public void update(IsoRenderer r, int deltaTime) {
        if(this.dropItem){
            int[] currentLoc = r.screenToIsometric(renderX, renderY);
            Chunk thisChunk = r.getChunkManager().getChunk(chunkX, chunkY);
            this.droppedItem.setLocation(currentLoc[0],currentLoc[1],currentLoc[2],currentLoc[3]);
            thisChunk.addGameObject(this.droppedItem);
        }
        super.update(r,deltaTime);
        if(!List.of(this.biomes).contains(r.getChunkManager().getBiomeForChunk(chunkX,chunkY))) this.removeHealth(1);
    }

    @Override
    public void removeHealth(int amount){
        long now = System.currentTimeMillis();
        if (now - lastDamageTime < damageCooldown) {
            return; // Only allow damage every 1 second
        }
        this.health -= amount;
        if(this.lod<2) Game.passiveMobSoundBox.playRandomSound("fishHurt");
        if (this.health <= 0) {
            this.dropItem = true;
            this.health = 0;
        }
        lastDamageTime = now;
        hurtFlashEndTime = now + HURT_FLASH_DURATION_MS; // Set flash timer
    }
}
