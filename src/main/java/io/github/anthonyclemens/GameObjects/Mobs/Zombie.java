package io.github.anthonyclemens.GameObjects.Mobs;

import java.util.Random;

import io.github.anthonyclemens.Achievements.AchievementType;
import io.github.anthonyclemens.GameObjects.Items;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Item;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.WorldGen.Biome;
import io.github.anthonyclemens.WorldGen.Chunk;

public class Zombie extends Mob{

    private final Item droppedItem;
    private boolean dropItem = false;

    public Zombie(int x, int y, int chunkX, int chunkY) {
        super("zombies", x, y, chunkX, chunkY, "zombie", 20);
        String spriteSheet = "zombies";
        this.animationIndex = 0;
        animationLoaders.put(Direction.UP, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex, 0, this.animationIndex, 2, 250));
        animationLoaders.put(Direction.DOWN, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+1, 0, this.animationIndex+1, 2, 250));
        animationLoaders.put(Direction.LEFT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+2, 0, this.animationIndex+2, 2, 250));
        animationLoaders.put(Direction.RIGHT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+3, 0, this.animationIndex+3, 2, 250));
        this.biomes = new Biome[]{Biome.FOREST, Biome.PLAINS, Biome.RAINFOREST};
        this.droppedItem = new Item(Items.ITEM_ZOMBIE_FLESH,x,y,chunkX,chunkY);
        this.droppedItem.setQuantity(new Random().nextInt(2)+1);
        this.intelligence = 0.75f;
        this.mobSpeed=2.5f;
        this.smoothness=0.01f;
        this.setSway(0f);
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
        if(r.isSunUp()) {
            this.removeHealth(this.rand.nextInt(3));
            this.mobSpeed=4f;
        }
    }

    @Override
    public void removeHealth(int amount){
        super.removeHealth(amount);
        if (this.health <= 0) {
            this.dropItem = true;
        }
    }

    @Override
    public void onHit(Player player, Items item) {
        this.removeHealth(item.getDamage());
        if(health<=0) player.getAchievementManager().recordProgress(AchievementType.ZOMBIE_SLAYING);
    }
}
