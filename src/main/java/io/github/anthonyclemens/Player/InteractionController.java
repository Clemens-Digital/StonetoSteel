package io.github.anthonyclemens.Player;

import java.util.Iterator;

import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Item;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Items;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.WorldGen.ChunkManager;

public class InteractionController {


    public InteractionController() {
    }


    public void interact(Input input, ChunkManager cm, Circle playerReach, Inventory playerInventory, Items equippedItem, IsoRenderer r) {
        int mouseX = input.getMouseX();
        int mouseY = input.getMouseY();

        if (!playerReach.contains(mouseX, mouseY) || !input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) return;

        int[] clickedLoc = r.screenToIsometric(mouseX, mouseY);
        Chunk chunk = cm.getChunk(clickedLoc[2], clickedLoc[3]);
        if (chunk == null) return;

        Iterator<GameObject> iterator = chunk.getGameObjects().iterator();
        while (iterator.hasNext()) {
            GameObject obj = iterator.next();
            if (!obj.getHitbox().contains(mouseX, mouseY)) continue;

            if (isItem(obj)) {
                Items itemType = Items.valueOf(obj.getName());
                int quantity = ((Item) obj).getQuantity();

                if (playerInventory.addItem(itemType, quantity)) {
                    iterator.remove();
                }
            } else {
                handleInteraction(obj, equippedItem);
            }
            chunk.setDirty(true);
        }
    }

    private boolean isItem(GameObject obj) {
        return obj.getName().startsWith("ITEM_") && obj instanceof Item;
    }

    private void handleInteraction(GameObject obj, Items equippedItem) {
        int damage = getDamageForItem(equippedItem);

        try {
            obj.removeHealth(damage);
        } catch (Exception e) {
            Log.debug("Error applying damage with " + equippedItem + " to " + obj.getName());
        }
    }

    private int getDamageForItem(Items item) {
        if(item==null) return 5;
        return switch (item) {
            case ITEM_WOODEN_SWORD -> 10;
            default -> 5;
        };
    }
}
