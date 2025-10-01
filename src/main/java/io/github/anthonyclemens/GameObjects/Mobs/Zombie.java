package io.github.anthonyclemens.GameObjects.Mobs;

import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.WorldGen.Biome;

public class Zombie extends Mob{

    public Zombie(int x, int y, int chunkX, int chunkY) {
        super("zombies", x, y, chunkX, chunkY, "zombie", 20);
        String spriteSheet = "zombies";
        this.animationIndex = 0;
        animationLoaders.put(Direction.UP, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex, 0, this.animationIndex, 2, 250));
        animationLoaders.put(Direction.DOWN, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+1, 0, this.animationIndex+1, 2, 250));
        animationLoaders.put(Direction.LEFT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+2, 0, this.animationIndex+2, 2, 250));
        animationLoaders.put(Direction.RIGHT, () -> SpriteManager.getAnimation(spriteSheet, this.animationIndex+3, 0, this.animationIndex+3, 2, 250));
        this.biomes = new Biome[]{Biome.FOREST, Biome.PLAINS, Biome.RAINFOREST};
        this.intelligence = 0.75f;
        this.mobSpeed=3f;
        this.smoothness=0.01f;
        this.setSway(0f);
        this.maxHealth = 20;
        this.health = this.maxHealth;
        this.peaceful = false;
    }

    @Override
    public void update(IsoRenderer r, int deltaTime){
        super.update(r,deltaTime);
        if(r.isSunUp()) {
            this.removeHealth(this.rand.nextInt(3));
            this.mobSpeed=4f;
        }
    }
}
