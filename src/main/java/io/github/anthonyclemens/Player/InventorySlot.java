package io.github.anthonyclemens.Player;

import java.io.Serializable;

import io.github.anthonyclemens.GameObjects.Items;

public class InventorySlot implements Serializable{
    private Items item;
    private int quantity;

    public InventorySlot() {
        this.item = null;
        this.quantity = 0;
    }

    public boolean isEmpty() {
        return item == null || quantity <= 0;
    }

    public Items getItem() { return item; }
    public int getQuantity() { return quantity; }

    public void setItem(Items item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public int getMaxStackSize() {
        if (item == null) return 0;
        return switch (item.getItemType()) {
            case AXE,HOE,PICKAXE,WEAPON -> 1;
            default -> 100;
        };
    }

    public int addItems(int amount) {
        if (item == null) return 0;
        int space = getMaxStackSize() - quantity;
        int toAdd = Math.min(space, amount);
        quantity += toAdd;
        return toAdd;
    }

    public int removeItems(int amount) {
        int toRemove = Math.min(amount, quantity);
        quantity -= toRemove;
        if (quantity <= 0) {
            item = null;
            quantity = 0;
        }
        return toRemove;
    }

    public void clear() {
        this.item = null;
        this.quantity = 0;
    }

}
