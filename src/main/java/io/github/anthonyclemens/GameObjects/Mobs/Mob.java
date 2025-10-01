package io.github.anthonyclemens.GameObjects.Mobs;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.Sys;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.GameObjects.SerializableSupplier;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.SharedData;
import io.github.anthonyclemens.Sound.SoundBox;
import io.github.anthonyclemens.WorldGen.Biome;
import io.github.anthonyclemens.WorldGen.ChunkManager;
import io.github.anthonyclemens.states.Game;

public class Mob extends GameObject {
    // Animation & Visual State
    protected transient Animation currentAnimation;
    protected Map<Direction, SerializableSupplier<Animation>> animationLoaders = new EnumMap<>(Direction.class);
    protected Direction currentDirection = Direction.DOWN;
    protected int animationIndex = 0;
    protected SerializableSupplier<SoundBox> soundLoader;
    protected transient SoundBox soundBox;
    protected Color colorOverlay;          // Optional color tint for the mob

    // Logical Positioning
    private transient float fx;                     // Internal float X position
    private transient float fy;                     // Internal float Y position

    // Pathfinding & Destination
    private transient int destinationX;            // Target X tile
    private transient int destinationY;            // Target Y tile
    private transient int nextDestinationX;            // Target X tile
    private transient int nextDestinationY;            // Target Y tile
    protected final Random rand;           // Seeded RNG for deterministic motion
    protected final int visionDistance; // Sets the vision distance for pathfinding
    protected float intelligence = 0; // Sets the percentage chance that the mob will lock onto the player

    // Sway & Movement Styling
    public enum MobState { IDLE, CHASE, ATTACK }
    private MobState state = MobState.IDLE;
    protected float distanceToDestination = 0f;
    protected float mobSpeed = 4f;       // Movement speed
    protected float currentSpeed = 0f;
    protected float smoothness = 0.15f;  // Easing factor for render interpolation
    protected transient float sway = 0f;        // Sway cycle range (for offset randomness)
    private transient float swayTime = 0f;         // Time accumulator for sine/cos motion
    private float swayOffset;            // Phase offset per mob for unique motion
    protected long lastDamageTime = 0; // Timestamp of last time damage was taken (milliseconds)
    protected long hurtFlashEndTime = 0; // Timestamp when hurt flash should end
    protected long damageCooldown = 1000; // Cooldown time between damage in milliseconds
    protected static final int HURT_FLASH_DURATION_MS = 250; // Duration of red flash in ms
    private long lastThinkTime = 0;
    private static final long THINK_INTERVAL_MS = 500;
    protected byte lod = 0;
    private Direction lastDirection = null;
    protected Biome[] biomes;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Mob(String tileSheet, int x, int y, int chunkX, int chunkY, String objName, int visionDistance) {
        super(tileSheet, x, y, chunkX, chunkY, objName);
        this.visionDistance = visionDistance;
        this.rand = new Random(SharedData.getSeed()+Sys.getTime());
        this.swayOffset = this.rand.nextFloat()* sway;
        this.fx = x;
        this.fy = y;
        this.alwaysCalcHitbox = true;
        this.destinationX=x;
        this.destinationY=y;
    }

    public void setSway(float newSway){
        this.swayOffset = this.rand.nextFloat()* newSway;
    }

    public void wander(IsoRenderer r) {
        int tries = 20;
        int chunkSize = ChunkManager.CHUNK_SIZE;

        while (tries-- > 0) {
            // Generate a candidate tile within vision range
            int candidateLocalX = this.x + rand.nextInt(visionDistance * 2 + 1) - visionDistance;
            int candidateLocalY = this.y + rand.nextInt(visionDistance * 2 + 1) - visionDistance;

            // Convert to global tile coordinates
            int globalTileX = candidateLocalX + this.chunkX * chunkSize;
            int globalTileY = candidateLocalY + this.chunkY * chunkSize;

            // Convert global tile to chunk coordinates
            int candidateChunkX = globalTileX / chunkSize;
            int candidateChunkY = globalTileY / chunkSize;

            // Convert global tile to local tile within its chunk
            int candidateTileX = globalTileX % chunkSize;
            int candidateTileY = globalTileY % chunkSize;

            // Check biome compatibility
            Biome candidateBiome = r.getChunkManager().getBiomeForChunk(candidateChunkX, candidateChunkY);

            if (List.of(this.biomes).contains(candidateBiome)) {
                setDestinationByGlobalPosition(new int[] {
                    candidateTileX,
                    candidateTileY,
                    candidateChunkX,
                    candidateChunkY
                });
                return;
            }
        }
        setDestinationByGlobalPosition(new int[] {
            this.x,
            this.y,
            this.chunkX,
            this.chunkY
        });
    }

    private void moveTowardsDestination(int deltaTime, IsoRenderer r) {
        switch(state){
            case IDLE -> {
                this.currentSpeed = this.mobSpeed;
                if(distanceToDestination < 1f){
                    wander(r);
                }
            }
            case CHASE -> {
                this.currentSpeed = this.mobSpeed*1.5f;
            }
            default -> {
            }
        }
        float dx = destinationX - fx;
        float dy = destinationY - fy;
        distanceToDestination = (float) Math.sqrt(dx * dx + dy * dy);

        float isoDx = dx;
        float isoDy = dy * 2;

        double rotatedX =  isoDx * Math.cos(-Math.PI/4) - isoDy * Math.sin(-Math.PI/4);
        double rotatedY =  isoDx * Math.sin(-Math.PI/4) + isoDy * Math.cos(-Math.PI/4);

        double angle = Math.atan2(rotatedY, rotatedX);

        if (angle >= -Math.PI / 4 && angle < Math.PI / 4) {
            currentDirection = Direction.RIGHT;
        } else if (angle >= Math.PI / 4 && angle < 3 * Math.PI / 4) {
            currentDirection = Direction.DOWN;
        } else if (angle >= -3 * Math.PI / 4 && angle < -Math.PI / 4) {
            currentDirection = Direction.UP;
        } else {
            currentDirection = Direction.LEFT;
        }

        float dirX = dx / distanceToDestination;
        float dirY = dy / distanceToDestination;

        fx += dirX * currentSpeed * deltaTime / 1000f;
        fy += dirY * currentSpeed * deltaTime / 1000f;

        int tileSize = ChunkManager.CHUNK_SIZE;

        int oldChunkX = chunkX;
        int oldChunkY = chunkY;
        int newChunkX = chunkX;
        int newChunkY = chunkY;
        // Wrap for left/right boundaries
        while (fx < 0) {
            newChunkX -= 1;
            fx += tileSize;
            destinationX += tileSize;
        }
        while (fx >= tileSize) {
            newChunkX += 1;
            fx -= tileSize;
            destinationX -= tileSize;
        }

        // Wrap for top/bottom boundaries
        while (fy < 0) {
            newChunkY -= 1;
            fy += tileSize;
            destinationY += tileSize;
        }
        while (fy >= tileSize) {
            newChunkY += 1;
            fy -= tileSize;
            destinationY -= tileSize;
        }
        if(newChunkX != oldChunkX || newChunkY != oldChunkY) {
            // This candidate is out of chunk, calculate move to new chunk
            this.chunkX = newChunkX;
            this.chunkY = newChunkY;
            r.getChunkManager().moveGameObjectToChunk(this, oldChunkX, oldChunkY, this.chunkX, this.chunkY);
        }
    }

    @Override
    public void update(IsoRenderer r, int deltaTime) {
        if (animationLoaders != null) {
            // Only update animation if direction changed
            if (currentAnimation == null || currentDirection != lastDirection) {
                SerializableSupplier<Animation> loader = animationLoaders.get(currentDirection);
                if (loader != null) {
                    currentAnimation = loader.get();
                    lastDirection = currentDirection;
                }
            }
        }

        swayTime += deltaTime;
        moveTowardsDestination(deltaTime, r);
        this.x = (int) fx;
        this.y = (int) fy;

        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
        }
    }

    @Override
    public void render(IsoRenderer r, int lodLevel) {
        this.lod=(byte)lodLevel;
        if (currentAnimation == null || r == null) return;

        if(System.currentTimeMillis() < hurtFlashEndTime){
            currentAnimation.draw(renderX, renderY,
            currentAnimation.getWidth() * r.getZoom(),
            currentAnimation.getHeight() * r.getZoom(), new Color(255, 0, 0, 120));
        }else{
            currentAnimation.draw(renderX, renderY,
            currentAnimation.getWidth() * r.getZoom(),
            currentAnimation.getHeight() * r.getZoom());
        }

        if (Game.showDebug&&this.hitbox!=null&&lodLevel<2) {
            switch(state){
                case IDLE -> r.getGraphics().setColor(Color.green);
                case CHASE -> r.getGraphics().setColor(Color.orange);
                case ATTACK -> r.getGraphics().setColor(Color.red);
            }
            r.getGraphics().drawString(state.name(), renderX, renderY);
            r.getGraphics().setColor((this.peaceful) ? Color.green : Color.red);
            r.getGraphics().draw(hitbox);

            float destRenderX = r.calculateIsoX(destinationX, destinationY, chunkX, chunkY);
            float destRenderY = r.calculateIsoY(destinationX, destinationY, chunkX, chunkY);

            r.getGraphics().setColor(Color.red);
            r.getGraphics().drawLine(renderX+r.getZoom()*this.currentAnimation.getWidth()/2, renderY+r.getZoom()*this.currentAnimation.getHeight()/2, destRenderX, destRenderY);

            r.getGraphics().setColor(Color.blue);
            r.getGraphics().fillOval(destRenderX - 3, destRenderY - 3, 6, 6);

            r.getGraphics().setColor(Color.black);
        }
    }

    public void setColorOverlay(Color color) {
        this.colorOverlay = color;
    }

    @Override
    public void removeHealth(int amount){
        long now = System.currentTimeMillis();
        if (now - lastDamageTime < damageCooldown) {
            return; // Only allow damage every 1 second
        }
        super.removeHealth(amount);
        lastDamageTime = now;
        hurtFlashEndTime = now + HURT_FLASH_DURATION_MS; // Set flash timer
    }

    public float getRenderX() { return renderX; }
    public float getRenderY() { return renderY; }
    public void setRenderX(float renderX) { this.renderX = renderX; }
    public void setRenderY(float renderY) { this.renderY = renderY; }

    @Override
    public void calculateHitbox(IsoRenderer r) {
        if (this.hitbox == null) this.hitbox = new Rectangle(0, 0, 0, 0);

        // Apply sway to logical position
        float swayX = (float) Math.sin((swayTime + swayOffset) / 300.0) * 0.3f;
        float swayY = (float) Math.cos((swayTime + swayOffset) / 400.0) * 0.3f;

        float swayedX = fx + swayX;
        float swayedY = fy + swayY;

        // Project swayed position to screen space
        float renderTargetX = r.calculateIsoX((int) swayedX, (int) swayedY, chunkX, chunkY);
        float renderTargetY = r.calculateIsoY((int) swayedX, (int) swayedY, chunkX, chunkY);

        // Interpolation factor: instant if camera is moving, smooth otherwise
        float interpolationFactor = r.isCameraMoving() ? 0.5f : smoothness;

        renderX += (renderTargetX - renderX) * interpolationFactor;
        renderY += (renderTargetY - renderY) * interpolationFactor;

        if (currentAnimation != null) {
            hitbox.setBounds(renderX, renderY,
                currentAnimation.getWidth() * r.getZoom(),
                currentAnimation.getHeight() * r.getZoom());
        }
    }

    public void think(long currentTime, Player player) {
        if (currentTime - lastThinkTime < THINK_INTERVAL_MS) return;
        lastThinkTime = currentTime;

        updateState(player);
    }

    private void updateState(Player player) {
        boolean neuronActivation = rand.nextFloat() <= this.intelligence;
        if (!neuronActivation) return;
        int distanceToPlayer = distanceToPlayer(player);
        if(distanceToPlayer > 2 && distanceToPlayer <= visionDistance){
            state = MobState.CHASE;
            setDestinationByGlobalPosition(player.getPlayerLocation());
        } else if(distanceToPlayer <= 2 && distanceToPlayer > 0){
            state = MobState.ATTACK;
            setDestinationByGlobalPosition(player.getPlayerLocation());
        } else {
            state = MobState.IDLE;
        }
        Log.debug("State: " + state + " distToPlayer: " + distanceToPlayer);
    }


    private int distanceToPlayer(Player player) {
        int mobWorldX = this.chunkX * ChunkManager.CHUNK_SIZE + this.x;
        int mobWorldY = this.chunkY * ChunkManager.CHUNK_SIZE + this.y;

        int[] playerPos = player.getPlayerLocation();
        int playerWorldX = playerPos[2] * ChunkManager.CHUNK_SIZE + playerPos[0];
        int playerWorldY = playerPos[3] * ChunkManager.CHUNK_SIZE + playerPos[1];

        int destChunkX = (chunkX * ChunkManager.CHUNK_SIZE + destinationX) / ChunkManager.CHUNK_SIZE;
        int destChunkY = (chunkY * ChunkManager.CHUNK_SIZE + destinationY) / ChunkManager.CHUNK_SIZE;



        Log.debug("Mob world position: (" + mobWorldX + ", " + mobWorldY + ")");
        Log.debug("Mob destination: (" + destinationX + ", " + destinationY + ")");
        Log.debug("Player world position: (" + playerWorldX + ", " + playerWorldY + ")" + " in chunk (" + destChunkX + ", " + destChunkY + ")");

        int dx = playerWorldX - mobWorldX;
        int dy = playerWorldY - mobWorldY;

        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    public void setDestinationByGlobalPosition(int[] globalLocation) {
        int chunkSize = ChunkManager.CHUNK_SIZE;

        int absoluteTargetX = globalLocation[2] * chunkSize + globalLocation[0];
        int absoluteTargetY = globalLocation[3] * chunkSize + globalLocation[1];

        int absoluteCurrentX = this.chunkX * chunkSize + this.x;
        int absoluteCurrentY = this.chunkY * chunkSize + this.y;

        int deltaX = absoluteTargetX - absoluteCurrentX;
        int deltaY = absoluteTargetY - absoluteCurrentY;

        this.destinationX = this.x + deltaX;
        this.destinationY = this.y + deltaY;
    }

    public int getVisionRadius(){
        return this.visionDistance;
    }

    public float getIntelligence(){
        return this.intelligence;
    }

    public boolean isPeaceful() {
        return this.peaceful;
    }
}


