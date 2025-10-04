package io.github.anthonyclemens.GameObjects.SingleTileObjects;

import java.util.Random;

import org.newdawn.slick.Color;

import io.github.anthonyclemens.GameObjects.Items;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.states.Game;

public class BerryBush extends SingleTileObject{

    private final long shakeDuration = 250; // When shaking should end
    private transient long lastDamageTime = 0; // Timestamp of last time damage was taken (milliseconds)
    private transient long endShakeTime = 0; // Timestamp when shaking should end
    private long damageCooldown = 500; // Cooldown time between damage in milliseconds
    private final int shakeAggression = 3; // How much the grass shakes when hit
    private transient float offsetX = 0; // Offset for shaking effect
    private transient float offsetY = 0; // Offset for shaking effect
    private final Random rand;
    private final Item droppedItem;
    private boolean dropItem = false;

    public BerryBush(Random rand, int x, int y, int chunkX, int chunkY) {
        super("main", "berryBush", 45, x, y, chunkX, chunkY);
        this.rand = rand;
        this.droppedItem = new Item(Items.ITEM_BERRIES, x, y, chunkX, chunkY);
        this.droppedItem.setQuantity(rand.nextInt(3)+1);
        this.solid=false;
        this.health=20;
        this.maxHealth=this.health;
    }

    @Override
    public void render(IsoRenderer r, int lodLevel) {
        renderX = r.calculateIsoX(x, y, chunkX, chunkY) + offsetX;
        renderY = r.calculateIsoY(x, y, chunkX, chunkY) + offsetY;
        if(this.hover){
            r.drawTileIso(tileSheet, i, renderX, renderY, new Color(0.7f, 0.7f, 0.7f, 1f));
        }else{
            r.drawTileIso(tileSheet, i, renderX, renderY);
        }
        if(Game.showDebug&&this.hitbox!=null&&r.getZoom()>=0.8f){
            r.getGraphics().setColor(Color.black);
            r.getGraphics().draw(hitbox);
        }
    }

    @Override
    public void update(IsoRenderer r, int deltaTime) {
        if(dropItem){
            Chunk thisChunk = r.getChunkManager().getChunk(chunkX, chunkY);
            thisChunk.addGameObject(this.droppedItem);
        }
        super.update(r, deltaTime);
        if(System.currentTimeMillis() < endShakeTime){
            offsetX = rand.nextInt(shakeAggression) - shakeAggression / 2f;
            offsetY = rand.nextInt(shakeAggression) - shakeAggression / 2f;
        }
    }

    @Override
    public void removeHealth(int amount) {
        long now = System.currentTimeMillis();
        if (now - lastDamageTime < damageCooldown) {
            return;
        }
        this.health -= amount;
        if (this.health <= 0) {
            dropItem = true;
            this.health = 0;
        }
        endShakeTime = now + shakeDuration;
        lastDamageTime = now;
        Game.gameObjectSoundBox.playRandomSound("smallTreeHitSounds");
    }

    @Override
    public void onHit(Player player, Items item){
        removeHealth(5);
    }
}
