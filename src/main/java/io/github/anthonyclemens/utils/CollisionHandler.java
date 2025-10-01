package io.github.anthonyclemens.utils;

import java.util.List;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.GameObjects.Mobs.Mob;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Item;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Items;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.states.Game;

public class CollisionHandler {
    public void checkPlayerCollision(Player player, Chunk currentChunk) {
        if (currentChunk == null) {
            Log.debug("Current chunk is null, skipping collision check.");
            return;
        }
        List<GameObject> gameObjects = currentChunk.getGameObjects();
        if (gameObjects == null || gameObjects.isEmpty()) {
            return;
        }
        for (GameObject gob : gameObjects) {
            if (gob.getHitbox()==null) return;
            if (gob.getHitbox().intersects(player.getHitbox())) {
                if(gob instanceof Mob){
                    switch(gob.getName()){
                        case "zombie" -> player.subtractHealth(currentChunk.getRandom().nextInt(6)+5);
                        case "spider" -> player.subtractHealth(currentChunk.getRandom().nextInt(10)+2);
                    }
                    Log.debug("Touching a "+gob.getName());
                    return;
                }
                if(!gob.isSolid()) return;
                Rectangle playerHit = player.getHitbox();
                Rectangle objectHit = gob.getHitbox();

                // Manually compute the intersection rectangle
                float intersectX = Math.max(playerHit.getX(), objectHit.getX());
                float intersectY = Math.max(playerHit.getY(), objectHit.getY());
                float intersectWidth = Math.min(playerHit.getX() + playerHit.getWidth(), objectHit.getX() + objectHit.getWidth()) - intersectX;
                float intersectHeight = Math.min(playerHit.getY() + playerHit.getHeight(), objectHit.getY() + objectHit.getHeight()) - intersectY;
                if (intersectWidth > 0 && intersectHeight > 0) {
                    if (gob.getName().startsWith("ITEM_")) {
                        Items itemType = Items.valueOf(gob.getName());
                        if(player.getPlayerInventory().addItem(itemType, ((Item) gob).getQuantity())){
                            currentChunk.removeGameObject(gob.getUUID());
                        }
                        return;
                    }
                    // Compute centers from hitboxes
                    float playerCenterX = playerHit.getX() + playerHit.getWidth() / 2;
                    float playerCenterY = playerHit.getY() + playerHit.getHeight() / 2;
                    float objectCenterX = objectHit.getX() + objectHit.getWidth() / 2;
                    float objectCenterY = objectHit.getY() + objectHit.getHeight() / 2;
                    float dx = player.getX() - player.getPreviousX();
                    float dy = player.getY() - player.getPreviousY();
                    if (intersectWidth < intersectHeight) {
                        // Horizontal collision resolution
                        if (playerCenterX < objectCenterX) {
                            // Player approaches from the left, push left.
                            player.setX(player.getX() - intersectWidth);
                        } else {
                            // Player approaches from the right, push right.
                            player.setX(player.getX() + intersectWidth);
                        }
                        // Simulate a bounce: reverse horizontal movement component.
                        player.setPreviousX(player.getX() + dx);
                    } else {
                        // Vertical collision resolution
                        if (playerCenterY < objectCenterY) {
                            // Player approaches from above, push upward.
                            player.setY(player.getY() - intersectHeight);
                        } else {
                            // Player approaches from below, push downward.
                            player.setY(player.getY() + intersectHeight);
                        }
                        // Simulate a bounce: reverse vertical movement component.
                        player.setPreviousY(player.getY() + dy);
                    }
                    switch(gob.getName()){
                        case "cactus" -> player.subtractHealth(10);
                    }
                    if (Game.showDebug) {
                        Log.debug("Collision detected with game object: " + gob.getName());
                    }
                }
            }
        }
    }
    public void checkMobCollision(Mob mob, Chunk currentChunk) {
        if (currentChunk == null) {
            Log.debug("Current chunk is null, skipping collision check.");
            return;
        }
        // Ensure mob render coordinates are valid before proceeding
        if (Float.isNaN(mob.getRenderX()) || Float.isNaN(mob.getRenderY())) {
            Log.debug("Mob render coordinates are invalid, skipping collision check.");
            return;
        }
        List<GameObject> gameObjects = currentChunk.getGameObjects();
        if (gameObjects == null || gameObjects.isEmpty()) {
            return;
        }
        for (GameObject gob : gameObjects) {
            if (gob != mob && gob.getHitbox().intersects(mob.getHitbox())) {
                Rectangle mobHit = mob.getHitbox();
                Rectangle otherHit = gob.getHitbox();
                
                // Manually compute the intersection rectangle
                float intersectX = Math.max(otherHit.getX(), mobHit.getX());
                float intersectY = Math.max(otherHit.getY(), mobHit.getY());
                float intersectWidth = Math.min(otherHit.getX() + otherHit.getWidth(), mobHit.getX() + mobHit.getWidth()) - intersectX;
                float intersectHeight = Math.min(otherHit.getY() + otherHit.getHeight(), mobHit.getY() + mobHit.getHeight()) - intersectY;
                if (intersectWidth > 0 && intersectHeight > 0) {
                    // Compute centers from hitboxes
                    float mobCenterX = mobHit.getX() + mobHit.getWidth() / 2;
                    float mobCenterY = mobHit.getY() + mobHit.getHeight() / 2;
                    float objectCenterX = otherHit.getX() + otherHit.getWidth() / 2;
                    float objectCenterY = otherHit.getY() + otherHit.getHeight() / 2;
                    float dx = mob.getRenderX() - mob.getPreviousX();
                    float dy = mob.getRenderY() - mob.getPreviousY();
                    if (intersectWidth < intersectHeight) {
                        // Horizontal collision resolution
                        if (mobCenterX < objectCenterX) {
                            // Mob approaches from the left, push left.
                            mob.setRenderX(mob.getRenderX() - intersectWidth);
                        } else {
                            // Mob approaches from the right, push right.
                            mob.setRenderX(mob.getRenderX() + intersectWidth);
                        }
                        // Simulate a bounce: reverse horizontal movement component.
                        mob.setPreviousX(mob.getRenderX() + dx);
                    } else {
                        // Vertical collision resolution
                        if (mobCenterY < objectCenterY) {
                            // Mob approaches from above, push upward.
                            mob.setRenderY(mob.getRenderY() - intersectHeight);
                        } else {
                            // Mob approaches from below, push downward.
                            mob.setRenderY(mob.getRenderY() + intersectHeight);
                        }
                        // Simulate a bounce: reverse vertical movement component.
                        mob.setPreviousY(mob.getRenderY() + dy);
                    }
                    
                    if (Game.showDebug) {
                        Log.debug("Collision detected with game object: " + gob.getName());
                    }
                }
            }
        }
    }
}


