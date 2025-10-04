package io.github.anthonyclemens.Player;

import java.util.List;
import java.util.Map;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.GameObjects.Items;
import io.github.anthonyclemens.GameObjects.SingleTileObjects.Item;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.Settings;
import io.github.anthonyclemens.Sound.SoundBox;
import io.github.anthonyclemens.Utils;
import io.github.anthonyclemens.WorldGen.Biome;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.WorldGen.World;
import io.github.anthonyclemens.states.Game;

public class Player {

    // Player location and movement properties
    private float x;
    private float y;
    private float dx;
    private float dy;
    private float previousX; // Previous X position
    private float previousY; // Previous Y position
    private float defaultSpeed; // Default movement speed
    private int direction; // Current look direction
    private boolean cameraLocked = true; // Lock camera to player when true
    private Animation[] animations = new Animation[8]; // Array of animations for 8 directions
    private Animation[] idleAnimations = new Animation[8];
    private float renderX;
    private float renderY;
    private final Rectangle hitbox; // Hitbox for collision detection
    private Line raycastLine = new Line(0,0,0,0); // Stores the raycast line as a Slick2D Line object
    private final Circle playerReach = new Circle(0,0,0); // Circle representing the player's reach
    private static final float REACH = 128f; // Length of the player's reach in base scale pixels
    private Inventory playerInventory = new Inventory();
    private Chunk currentChunk;
    private int[] playerLoc; // Player location in the world [x, y, chunkX, chunkY]
    private final InteractionController interactor;
    private Items equippedItem;
    private transient IsoRenderer renderer;

    // Player health properties
    private byte health; // Player health
    private byte maxHealth; // Maximum health
    private long lastDamageTime = 0; // Timestamp of last time damage was taken (milliseconds)
    private long hurtFlashEndTime = 0; // Timestamp when hurt flash should end
    private static final int HURT_FLASH_DURATION_MS = 250; // Duration of red flash in ms

    // Player sound properties
    private final SoundBox playerSoundBox; // SoundBox for player sounds
    // Grass
    private final List<String> grassWalk = Utils.getFilePaths("sounds/Player/Walk/Grass/walk", 1, 8); // Grass walk sounds
    private final List<String> grassRun = Utils.getFilePaths("sounds/Player/Run/Grass/run", 1, 8); // Grass walk sounds
    // Water
    private final List<String> waterWalk = Utils.getFilePaths("sounds/Player/Walk/Water/walk", 1, 8); // Water walk sounds
    // Sand
    private final List<String> sandWalk = Utils.getFilePaths("sounds/Player/Walk/Sand/walk", 1, 8); // Sand walk sounds
    private final List<String> sandRun = Utils.getFilePaths("sounds/Player/Run/Sand/run", 5, 8); // Sand walk sounds
    // Ouch sound
    private final List<String> ouch = Utils.getFilePaths("sounds/Player/Hurt/ouch", 1, 2); // Ouch sounds

    public Player(float startX, float startY, float speed) {
        Settings settings = Settings.getInstance(); // Get settings instance
        loadAnimations();
        this.x = startX;
        this.y = startY;
        this.maxHealth = 100; // Initialize max health
        this.health = this.maxHealth; // Initialize health
        this.defaultSpeed = speed;
        this.playerSoundBox = new SoundBox(); // Initialize SoundBox
        this.interactor = new InteractionController();
        if(animations[0] != null) {
            // Initialize hitbox based on the first animation's dimensions
            this.hitbox = new Rectangle(this.x, this.y, animations[0].getWidth(), animations[0].getHeight());
            this.playerSoundBox.addSounds("grassWalk", grassWalk); // Add walk sounds to SoundBox
            this.playerSoundBox.addSounds("grassRun", grassRun); // Add run sounds to SoundBox
            this.playerSoundBox.addSounds("waterWalk", waterWalk); // Add water sounds to SoundBox

            this.playerSoundBox.addSounds("sandWalk", sandWalk); // Add sand sounds to SoundBox
            this.playerSoundBox.addSounds("sandRun", sandRun); // Add sand sounds to SoundBox

            this.playerSoundBox.addSounds("ouch", ouch); // Add ouch sounds to SoundBox

            this.playerSoundBox.setVolume(settings.getPlayerVolume()*settings.getMainVolume()); // Set volume for player sounds
        } else {
            // Fallback if animations are not provided
            Log.warn("Animations not provided or empty. Using default hitbox size.");
            this.hitbox = new Rectangle(this.x, this.y, 16, 16); // Default size
        }
    }

    public void update(Input input, int delta, int[] playerLoc, Chunk chunk, boolean paused) {
        if(animations[0] == null || idleAnimations[0] == null) loadAnimations();
        toggleCameraLock(input);
        if(paused) return;
        this.currentChunk = chunk;
        this.playerLoc = playerLoc;
        previousX = x; // Store current X position as previous
        previousY = y; // Store current Y position as previous
        dx = 0;
        dy = 0;
        float speed = this.defaultSpeed;
        String block = getBlockType(currentChunk.getTile(playerLoc[0], playerLoc[1])); // Get block type player is on

        handleMovementInput(input);
        speed = adjustSpeedAndAnimation(input, block, speed);

        if (dx != 0 || dy != 0) {
            normalizeAndMove(delta, speed);
            direction = updateDirection(dx, dy);
            animations[direction].start(); // Play movement animation
            playMovementSound(block, speed);
        } else {
            idleAnimations[direction].start(); // Play idle animation
        }
    }

    private void handleMovementInput(Input input) {
        if (input.isKeyDown(Input.KEY_W)) dy -= 1; // Up
        if (input.isKeyDown(Input.KEY_S)) dy += 1; // Down
        if (input.isKeyDown(Input.KEY_A)) dx -= 1; // Left
        if (input.isKeyDown(Input.KEY_D)) dx += 1; // Right
    }

    private float adjustSpeedAndAnimation(Input input, String block, float speed) {
        if (input.isKeyDown(Input.KEY_LSHIFT)) {
            speed = this.defaultSpeed * 1.5f;
            animations[direction].setSpeed(1.5f); // Increase animation speed for running
        } else {
            animations[direction].setSpeed(1f); // Reset animation speed for walking
        }
        if (block.equals("water")) {
            speed *= 0.5f; // Slow down on water
            animations[direction].setSpeed(0.5f); // Slow down animation speed for water
        }
        return speed;
    }

    private void normalizeAndMove(int delta, float speed) {
        dy *= 0.5f;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            dx /= length;
            dy /= length;
        }
        this.x += dx * speed * delta;
        this.y += dy * speed * delta;
    }

    private void playMovementSound(String block, float speed) {
        if (!this.playerSoundBox.isAnySoundPlaying()) {
            if (speed <= this.defaultSpeed) {
                this.playerSoundBox.playRandomSound(block + "Walk"); // Play walk sound
            } else {
                this.playerSoundBox.playRandomSound(block + "Run"); // Play run sound
            }
        }
    }

    private void toggleCameraLock(Input input) {
        if (input.isKeyPressed(Input.KEY_SPACE)) {
            cameraLocked = !cameraLocked;
        }
    }

    public int updateDirection(float dx, float dy) {
        if (dx == 0 && dy == 0) return 0; // Default to Up if no movement

        int xDir = (dx > 0) ? 1 : (dx < 0) ? -1 : 0;
        int yDir = (dy > 0) ? 1 : (dy < 0) ? -1 : 0;

        // Direction lookup: [y+1][x+1]
        int[][] directionTable = {
            {7, 0, 1}, // dy < 0: Up-left, Up, Up-right
            {6, -1, 2}, // dy == 0: Left, -, Right
            {5, 4, 3}  // dy > 0: Down-left, Down, Down-right
        };

        int dir = directionTable[yDir + 1][xDir + 1];
        return (dir == -1) ? 0 : dir;
    }

    public void render(GameContainer container, float zoom, float cameraX, float cameraY, IsoRenderer r) {
        if(animations == null || idleAnimations == null || r == null) return;
        renderer = r;
        renderX = (x - cameraX) * zoom + container.getWidth() / 2f;
        renderY = (y - cameraY) * zoom + container.getHeight() / 2f;
        boolean flashRed = System.currentTimeMillis() < hurtFlashEndTime;
        if (dx == 0 && dy == 0) {
            idleAnimations[direction].draw(renderX, renderY, animations[direction].getWidth() * zoom, animations[direction].getHeight() * zoom); // Render idle animation
        } else {
            animations[direction].draw(renderX, renderY, animations[direction].getWidth() * zoom, animations[direction].getHeight() * zoom); // Render movement animation
        }
        if (flashRed) {
            idleAnimations[direction].draw(renderX, renderY, animations[direction].getWidth() * zoom, animations[direction].getHeight() * zoom, new Color(255, 0, 0, 120));
        }
        if(Game.showDebug && raycastLine != null) {
            container.getGraphics().draw(hitbox);
            container.getGraphics().draw(raycastLine);
            container.getGraphics().draw(playerReach);
        }
        hitbox.setBounds(renderX, renderY, (animations[direction].getWidth()*zoom), (animations[direction].getHeight()*zoom));
        // Update the raycast line to come out of the middle of the character hitbox and face the direction
        float centerX = renderX + (animations[direction].getWidth() * zoom) / 2f;
        float centerY = renderY + (animations[direction].getHeight() * zoom) / 2f;
        float rayDx = 0;
        float rayDy = 0;
        switch (direction) {
            case 0 -> { rayDx = 0; rayDy = -1; } // Up
            case 1 -> { rayDx = 1; rayDy = -1; } // Up-right
            case 2 -> { rayDx = 1; rayDy = 0; } // Right
            case 3 -> { rayDx = 1; rayDy = 1; } // Down-right
            case 4 -> { rayDx = 0; rayDy = 1; } // Down
            case 5 -> { rayDx = -1; rayDy = 1; } // Down-left
            case 6 -> { rayDx = -1; rayDy = 0; } // Left
            case 7 -> { rayDx = -1; rayDy = -1; } // Up-left
        }
        // Normalize direction
        float length = (float)Math.sqrt(rayDx * rayDx + rayDy * rayDy);
        if (length != 0) {
            rayDx /= length;
            rayDy /= length;
        }
        float rayLength = REACH * zoom; // Length of the raycast line, scaled by zoom
        float endX = centerX + rayDx * rayLength;
        float endY = centerY + rayDy * rayLength;
        playerReach.setCenterX(centerX);
        playerReach.setCenterY(centerY);
        playerReach.setRadius(rayLength); // Set the radius of the player's reach circle
        raycastLine = new Line(centerX, centerY, endX, endY);
    }

    private String getBlockType(int tile){
        if (tile >= 0 && tile <= 4 || tile >= 10 && tile <= 14) {
            return "grass";
        } else if (tile >= 5 && tile <= 6) {
            return "sand";
        } else if (tile >= 23 && tile <= 24) {
            return "water";
        } else if (tile >= 50 && tile <= 60) {
            return "stone";
        } else {
            return "grass"; // Default to grass if tile type is unknown
        }
    }

    public void setVolume(float volume) {
        this.playerSoundBox.setVolume(volume); // Set volume for player sounds
    }

    public boolean isCameraLocked() {
        return cameraLocked;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRenderX(){
        return renderX;
    }

    public float getRenderY(){
        return renderY;
    }

    public float getPreviousX() {
        return previousX; // Get previous X position
    }

    public float getPreviousY() {
        return previousY; // Get previous Y position
    }

    public void setX(float newX) {
        this.x = newX; // Set player X position
    }

    public void setY(float newY) {
        this.y = newY; // Set player Y position
    }

    public Rectangle getHitbox(){
        return this.hitbox; // Get player hitbox
    }

    public float getSpeed(){
        return this.defaultSpeed; // Get player speed
    }

    public int getHealth() {
        return health; // Get player health
    }

    public void setHealth(int health) {
        if(health > this.maxHealth){
            this.health = this.maxHealth; // Set player health to max if it exceeds
        } else {
            this.health = (byte) health; // Set player health
        }
    }

    public void setSpeed(float speed) {
        this.defaultSpeed = speed; // Set player speed
    }

    public void addHealth(int add) {
        if(this.health+add > this.maxHealth){
            this.health = this.maxHealth; // Set player health to max if it exceeds
        } else {
            this.health += add; // Add health to player
        }
    }

    public void subtractHealth(int damage) {
        long now = System.currentTimeMillis();
        if (now - lastDamageTime < 1000) {
            return; // Only allow damage every 1 second
        }
        lastDamageTime = now;
        hurtFlashEndTime = now + HURT_FLASH_DURATION_MS; // Set flash timer
        if(this.health-damage <= 0){
            this.reset(); // Reset player state if health drops to 0
        } else {
            this.cameraLocked = true;
            this.health -= damage; // Subtract health from player
        }
        if(!this.playerSoundBox.getCurrentCategory().equals("ouch")) {
            this.playerSoundBox.playRandomSound("ouch"); // Play ouch sound when health is subtracted
        }
    }

    public int getMaxHealth() {
        return maxHealth; // Get player max health
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = (byte) maxHealth; // Set player max health
    }

    public void setPreviousX(float newX){
        this.previousX=newX;
    }

    public void setPreviousY(float newY){
        this.previousY=newY;
    }

    public Line getRaycastLine() {
        return raycastLine; // Getter for the raycast line
    }

    public int getDirection() {
        return direction; // Get player's current direction
    }

    public String getSound() {
        return playerSoundBox.getCurrentSound(); // Get the currently playing sound
    }

    public Inventory getPlayerInventory() {
        return playerInventory; // Get player's inventory
    }

    public void setPlayerInventory(Inventory newInv){
        this.playerInventory=newInv;
    }

    /**
     * Resets the player's stats and state to default values.
     */
    public void reset() {
        Map<Items,Integer> dropped = playerInventory.clearAndReturnItems();
        for (Map.Entry<Items,Integer> entry : dropped.entrySet()) {
            Items item = entry.getKey();
            int quantity = entry.getValue();

            Item itemToDrop = new Item(item, playerLoc[0], playerLoc[1], playerLoc[2], playerLoc[3]);
            itemToDrop.setQuantity(quantity);
            currentChunk.addGameObject(itemToDrop);
        }
        this.x = 0;
        this.y = 0;
        this.dx = 0;
        this.dy = 0;
        this.previousX = 0;
        this.previousY = 0;
        this.health = 100;
        this.maxHealth = 100;
        this.direction = 0;
        this.cameraLocked = true;
        this.renderX = 0;
        this.renderY = 0;
        this.hitbox.setBounds(0, 0, animations[0].getWidth(), animations[0].getHeight());
        this.lastDamageTime = 0;
    }

    public void interact(Input input, World cm){
        interactor.interact(input, cm, playerReach, playerInventory, this, renderer);
    }

    // Add getter/setter for equipped item so other systems may change it
    public Items getEquippedItem() {
        return Items.ITEM_WOODEN_AXE; // Placeholder until equipment system is implemented
        //return this.equippedItem;
    }

    public void setEquippedItem(Items item) {
        this.equippedItem = item;
    }

    private void loadAnimations(){
        int animationDuration = 140; // ms per frame

        // Movement animations (row 0 -> row 2)
        for (int dir = 0; dir < 8; dir++) {
            this.animations[dir] = SpriteManager.getAnimation(
                "player",
                dir, 0,   // start frame col/row
                dir, 2,   // end frame col/row
                animationDuration
            );
        }

        // Idle animations (row 1 -> row 1, only 1 frame)
        for (int dir = 0; dir < 8; dir++) {
            this.idleAnimations[dir] = SpriteManager.getAnimation(
                "player",
                dir, 1,
                dir, 1,
                animationDuration
            );
        }
    }

    /**
     * Retrieves player location
     * @return int[] containing x, y, chunkX, and chunkY
     */
    public int[] getPlayerLocation(){
        return this.playerLoc;
    }

    public Biome getBiome(){
        if(this.currentChunk==null) return null;
        return this.currentChunk.getBiome();
    }

    public InteractionController getInteractor(){
        return this.interactor;
    }

    public Chunk getCurrentChunk() {
        return this.currentChunk;
    }
}
