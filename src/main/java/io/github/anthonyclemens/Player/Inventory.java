package io.github.anthonyclemens.Player;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import io.github.anthonyclemens.GameObjects.Items;

public class Inventory implements Serializable {

    // --- Inventory configuration ---
    private static final int ROWS = 4;
    private static final int COLS = 9;
    private static final int HOTBAR_SIZE = 9;

    private final InventorySlot[] slots = new InventorySlot[ROWS * COLS];
    private int selectedHotbarSlot = 0; // 0–8

    public Inventory() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new InventorySlot();
        }
    }

    // --- Hotbar methods ---
    public void selectHotbarSlot(int index) {
        if (index >= 0 && index < HOTBAR_SIZE) {
            selectedHotbarSlot = index;
        }
    }

    public InventorySlot getSelectedSlot() {
        return slots[selectedHotbarSlot];
    }

    public Items getEquippedItem() {
        InventorySlot slot = getSelectedSlot();
        return slot.isEmpty() ? null : slot.getItem();
    }

    /** Add items into the inventory, stacking where possible. */
    public boolean addItem(Items itemType, int quantity) {
        int remaining = quantity;

        // Try stacking into existing slots
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem() == itemType) {
                int added = slot.addItems(remaining);
                remaining -= added;
                if (remaining <= 0) return true;
            }
        }

        // Try empty slots
        for (InventorySlot slot : slots) {
            if (slot.isEmpty()) {
                slot.setItem(itemType, 0);
                int added = slot.addItems(remaining);
                remaining -= added;
                if (remaining <= 0) return true;
            }
        }

        return remaining < quantity; // true if at least some were added
    }

    /** Get total count of a specific item across all slots. */
    public int getItemCount(Items itemType) {
        return Arrays.stream(slots)
                .filter(s -> !s.isEmpty() && s.getItem() == itemType)
                .mapToInt(InventorySlot::getQuantity)
                .sum();
    }

    /** Get total count of all items. */
    public int getTotalItemCount() {
        return Arrays.stream(slots)
                .filter(s -> !s.isEmpty())
                .mapToInt(InventorySlot::getQuantity)
                .sum();
    }

    /** Return a synthetic Map view of items (for compatibility). */
    public Map<Items, Integer> getItems() {
        Map<Items, Integer> result = new EnumMap<>(Items.class);
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                result.merge(slot.getItem(), slot.getQuantity(), Integer::sum);
            }
        }
        return result;
    }

    /** Remove a quantity of an item across slots. */
    public void removeItem(Items itemType, int quantity) {
        int remaining = quantity;
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItem() == itemType) {
                int removed = slot.removeItems(remaining);
                remaining -= removed;
                if (remaining <= 0) break;
            }
        }
    }

    /** Clear inventory and return snapshot of items (for drops). */
    public Map<Items, Integer> clearAndReturnItems() {
        Map<Items, Integer> snapshot = getItems();
        for (InventorySlot slot : slots) {
            slot.clear();
        }
        return snapshot;
    }
}