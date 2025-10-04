package io.github.anthonyclemens.Player;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.anthonyclemens.GameObjects.Items;

public class InventoryTest {
    private Inventory inventory;

    @Before
    public void setup(){
        inventory = new Inventory();
    }

    @Test
    public void testQuantityCheck(){
        inventory.addItem(Items.ITEM_WOOD, 64);
        inventory.addItem(Items.ITEM_WOOD, 3);
        inventory.addItem(Items.ITEM_CACTUS,32);
        inventory.addItem(Items.ITEM_STONE,13);
        Assert.assertEquals("Checking ITEM_WOOD 64 + 3 = 67", 64+3, inventory.getItemCount(Items.ITEM_WOOD));
        Assert.assertEquals("Checking ITEM_CACTUS is 32", 32, inventory.getItemCount(Items.ITEM_CACTUS));
        Assert.assertEquals("Checking ITEM_STONE 13", 13, inventory.getItemCount(Items.ITEM_STONE));
    }

    @Test
    public void testFullSlot(){
        inventory.addItem(Items.ITEM_WOOD, 98);
        boolean notFull = inventory.addItem(Items.ITEM_WOOD, 3);
        Assert.assertTrue("Make sure that that the inventory goes up to full amount and not over", notFull);
        //notFull = inventory.addItem(Items.ITEM_WOOD, 2);
        //Assert.assertFalse("Make sure that false is returned when inventory is full", notFull);
    }


}
