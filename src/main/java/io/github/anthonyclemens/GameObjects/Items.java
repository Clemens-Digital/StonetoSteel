package io.github.anthonyclemens.GameObjects;

public enum Items {
    ITEM_WOOD("items", 1, ItemType.MATERIAL),
    ITEM_STONE("items", 0, ItemType.MATERIAL),
    ITEM_CACTUS("main", 9, ItemType.MISC),
    ITEM_FISH("fish", 0, ItemType.FOOD),
    ITEM_SEED("items", 3, ItemType.SEED),
    ITEM_ZOMBIE_FLESH("items", 0, ItemType.FOOD),
    ITEM_BERRIES("items", 4, ItemType.FOOD),
    ITEM_STRING("items", 0, ItemType.MATERIAL),
    ITEM_BONES("items", 2, ItemType.MATERIAL),
    ITEM_WOODEN_SWORD("weapons", 0, ItemType.WEAPON, 5, 1.2f),
    ITEM_WOODEN_AXE("weapons", 12, ItemType.AXE, 7, 1.0f),
    ITEM_WOODEN_HOE("tools", 0, ItemType.HOE, 1, 1.0f);

    private final String spriteSheet;
    private final int spriteIndex;
    private final ItemType itemType;
    private final int damage;
    private final float speed;

    // Constructor for items with a default damage and speed of 1
    Items(String spriteSheet, int spriteIndex, ItemType itemType) {
        this(spriteSheet, spriteIndex, itemType, 1, 1f);
    }

    // Constructor for weapons/tools
    Items(String spriteSheet, int spriteIndex, ItemType itemType, int damage, float speed) {
        this.spriteSheet = spriteSheet;
        this.spriteIndex = spriteIndex;
        this.itemType = itemType;
        this.damage = damage;
        this.speed = speed;
    }

    public String getSpriteSheet() {
        return spriteSheet;
    }

    public int getSpriteIndex() {
        return spriteIndex;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public int getDamage() {
        return damage;
    }

    public float getSpeed() {
        return speed;
    }
}