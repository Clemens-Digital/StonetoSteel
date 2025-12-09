package io.github.anthonyclemens.GameObjects.Mobs;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.lwjgl.Sys;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.GameObjects.Items;
import io.github.anthonyclemens.GameObjects.SerializableSupplier;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.SharedData;
import io.github.anthonyclemens.Sound.SoundBox;
import io.github.anthonyclemens.WorldGen.Biome;
import io.github.anthonyclemens.WorldGen.World;
import io.github.anthonyclemens.states.Game;

public class Mob extends GameObject {
    // Animation & Visual State
    protected transient Animation currentAnimation;
    protected Map<Direction, SerializableSupplier<Animation>> animationLoaders = new EnumMap<>(Direction.class);
    protected Direction currentDirection = Direction.DOWN;
    protected int animationIndex = 0;
    protected SerializableSupplier<SoundBox> soundLoader;
    protected transient SoundBox soundBox;
    protected Color colorOverlay;

    // Logical Positioning (local to chunk)
    private transient float fx;
    private transient float fy;

    // A* Path
    private Queue<int[]> path = new LinkedList<>(); // absolute tile steps
    private long lastPathComputeTime = 0;
    private static final long PATH_REPLAN_MS = 600;

    // Movement & AI
    public enum MobState { IDLE, CHASE, ATTACK }
    private MobState state = MobState.IDLE;
    protected float mobSpeed = 4f;
    protected float currentSpeed = 0f;
    protected float smoothness = 0.15f;

    // Sway for render interpolation
    protected transient float sway = 0f;
    private transient float swayTime = 0f;
    private float swayOffset;

    // Damage flash
    protected long lastDamageTime = 0;
    protected long hurtFlashEndTime = 0;
    protected long damageCooldown = 1000;
    protected static final int HURT_FLASH_DURATION_MS = 250;

    // Thinking cadence
    private long lastThinkTime = 0;
    private static final long THINK_INTERVAL_MS = 500;

    // Perception
    protected final Random rand;
    protected final int visionDistance;
    protected float intelligence = 0; // 0..1 probability of reacting
    protected byte lod = 0;
    protected Biome[] biomes;

    private Direction lastDirection = null;

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    public Mob(String tileSheet, int x, int y, int chunkX, int chunkY, String objName, int visionDistance) {
        super(tileSheet, x, y, chunkX, chunkY, objName);
        this.visionDistance = visionDistance;
        this.rand = new Random(SharedData.getSeed() + Sys.getTime());
        this.swayOffset = this.rand.nextFloat() * sway;
        this.fx = x;
        this.fy = y;
        this.alwaysCalcHitbox = true;
    }

    public void setSway(float newSway) {
        this.swayOffset = this.rand.nextFloat() * newSway;
    }

    @Override
    public void update(IsoRenderer r, int deltaTime) {
        // Refresh animation only if direction changed
        if (animationLoaders != null && (currentAnimation == null || currentDirection != lastDirection)) {
            SerializableSupplier<Animation> loader = animationLoaders.get(currentDirection);
            if (loader != null) {
                currentAnimation = loader.get();
                lastDirection = currentDirection;
            }
        }

        swayTime += deltaTime;
        followPath(deltaTime, r);
        this.x = (int) fx;
        this.y = (int) fy;

        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
        }
    }

    private void followPath(int deltaTime, IsoRenderer r) {
        if (path == null || path.isEmpty()) return;

        int[] targetAbs = path.peek(); // absolute tile coords

        // Convert mob local (fx, fy, chunkX, chunkY) to absolute
        int tileSize = World.CHUNK_SIZE;
        float absX = this.chunkX * tileSize + fx;
        float absY = this.chunkY * tileSize + fy;

        float dx = targetAbs[0] - absX;
        float dy = targetAbs[1] - absY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Direction from iso space for animation
        updateFacingFromDelta(dx, dy);

        if (dist < 0.15f) {
            path.poll();
            return;
        }

        // Movement
        currentSpeed = switch (state) {
            case IDLE -> mobSpeed;
            case CHASE -> mobSpeed * 1.5f;
            case ATTACK -> mobSpeed * 1.2f;
        };

        float dirX = dx / dist;
        float dirY = dy / dist;

        absX += dirX * currentSpeed * deltaTime / 1000f;
        absY += dirY * currentSpeed * deltaTime / 1000f;

        // Wrap into chunk/local coords
        int newChunkX = (int) Math.floor(absX / tileSize);
        int newChunkY = (int) Math.floor(absY / tileSize);
        float newLocalX = absX - newChunkX * tileSize;
        float newLocalY = absY - newChunkY * tileSize;

        if (newChunkX != this.chunkX || newChunkY != this.chunkY) {
            int oldChunkX = this.chunkX;
            int oldChunkY = this.chunkY;
            this.chunkX = newChunkX;
            this.chunkY = newChunkY;
            r.getChunkManager().moveGameObjectToChunk(this, oldChunkX, oldChunkY, this.chunkX, this.chunkY);
        }

        this.fx = newLocalX;
        this.fy = newLocalY;
    }

    private void updateFacingFromDelta(float dx, float dy) {
        // Convert to iso for 4-direction facing
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
    }

    @Override
    public void render(IsoRenderer r, int lodLevel) {
        this.lod = (byte) lodLevel;
        if (currentAnimation == null || r == null) return;

        if (System.currentTimeMillis() < hurtFlashEndTime) {
            currentAnimation.draw(renderX, renderY,
                currentAnimation.getWidth() * r.getZoom(),
                currentAnimation.getHeight() * r.getZoom(),
                new Color(255, 0, 0, 120));
        } else {
            currentAnimation.draw(renderX, renderY,
                currentAnimation.getWidth() * r.getZoom(),
                currentAnimation.getHeight() * r.getZoom());
        }

        if (Game.showDebug && this.hitbox != null && lodLevel < 2) {
            r.getGraphics().drawString(state.name(), renderX, renderY);
            r.getGraphics().setColor((this.peaceful) ? Color.green : Color.red);
            r.getGraphics().draw(hitbox);
            switch (state) {
                case IDLE -> r.getGraphics().setColor(Color.green);
                case CHASE -> r.getGraphics().setColor(Color.orange);
                case ATTACK -> r.getGraphics().setColor(Color.red);
            }

            // Debug: draw current path
            if (path != null && !path.isEmpty()) {
                int lastX = (int) (renderX + currentAnimation.getWidth() * r.getZoom() / 2);
                int lastY = (int) (renderY + currentAnimation.getHeight() * r.getZoom() / 2);

                for (int[] stepAbs : path) {
                    int[] bc = r.getChunkManager().getBlockAndChunk(stepAbs[0], stepAbs[1]);
                    float stepRenderX = r.calculateIsoX(bc[0], bc[1], bc[2], bc[3]);
                    float stepRenderY = r.calculateIsoY(bc[0], bc[1], bc[2], bc[3]);

                    r.getGraphics().drawLine(lastX, lastY, stepRenderX, stepRenderY);
                    r.getGraphics().fillOval(stepRenderX - 2, stepRenderY - 2, 4, 4);

                    lastX = (int) stepRenderX;
                    lastY = (int) stepRenderY;
                }
            }

            r.getGraphics().setColor(Color.black);
        }
    }

    public void setColorOverlay(Color color) {
        this.colorOverlay = color;
    }

    @Override
    public void removeHealth(int amount) {
        long now = System.currentTimeMillis();
        if (now - lastDamageTime < damageCooldown) {
            return;
        }
        super.removeHealth(amount);
        lastDamageTime = now;
        hurtFlashEndTime = now + HURT_FLASH_DURATION_MS;
    }

    public float getRenderX() { return renderX; }
    public float getRenderY() { return renderY; }
    public void setRenderX(float renderX) { this.renderX = renderX; }
    public void setRenderY(float renderY) { this.renderY = renderY; }

    @Override
    public void calculateHitbox(IsoRenderer r) {
        if (this.hitbox == null) this.hitbox = new Rectangle(0, 0, 0, 0);

        float swayX = (float) Math.sin((swayTime + swayOffset) / 300.0) * 0.3f;
        float swayY = (float) Math.cos((swayTime + swayOffset) / 400.0) * 0.3f;
        float swayedX = fx + swayX;
        float swayedY = fy + swayY;

        float renderTargetX = r.calculateIsoX((int) swayedX, (int) swayedY, chunkX, chunkY);
        float renderTargetY = r.calculateIsoY((int) swayedX, (int) swayedY, chunkX, chunkY);

        float interpolationFactor = r.isCameraMoving() ? 0.5f : smoothness;

        renderX += (renderTargetX - renderX) * interpolationFactor;
        renderY += (renderTargetY - renderY) * interpolationFactor;

        if (currentAnimation != null) {
            hitbox.setBounds(renderX, renderY,
                currentAnimation.getWidth() * r.getZoom(),
                currentAnimation.getHeight() * r.getZoom());
        }
    }


    public void think(long currentTime, Player player, World world) {
        if (currentTime - lastThinkTime < THINK_INTERVAL_MS) return;
        lastThinkTime = currentTime;

        updateState(player, world);
    }

    public void wander(World world) {
        int tries = 20;
        int chunkSize = World.CHUNK_SIZE;

        while (tries-- > 0) {
            // Pick a random absolute tile within vision range
            int mobAbsX = this.chunkX * chunkSize + this.x;
            int mobAbsY = this.chunkY * chunkSize + this.y;

            int candidateAbsX = mobAbsX + rand.nextInt(visionDistance * 2 + 1) - visionDistance;
            int candidateAbsY = mobAbsY + rand.nextInt(visionDistance * 2 + 1) - visionDistance;

            if (Pathfinder.findPath(world, mobAbsX, mobAbsY, candidateAbsX, candidateAbsY).isEmpty()) {
                continue;
            }

            List<int[]> newPath = Pathfinder.findPath(world, mobAbsX, mobAbsY, candidateAbsX, candidateAbsY);
            if (!newPath.isEmpty()) {
                path.clear();
                path = new LinkedList<>(newPath);
                int[] head = path.peek();
                if (head != null && head[0] == mobAbsX && head[1] == mobAbsY) {
                    path.poll();
                }
                return;
            }
        }
    }

    private void updateState(Player player, World world) {
        if (this.peaceful) {
            state = MobState.IDLE;
            if (path == null || path.isEmpty()) {
                wander(world);
            }
            return;
        }

        // Hostile AI
        boolean neuronActivation = rand.nextFloat() <= this.intelligence;
        if (!neuronActivation) return;

        int distToPlayer = distanceToPlayer(player);
        if (distToPlayer > 2 && distToPlayer <= visionDistance) {
            state = MobState.CHASE;
            maybeReplanPathToPlayer(player, world);
        } else if (distToPlayer <= 2 && distToPlayer > 0) {
            state = MobState.ATTACK;
            maybeReplanPathToPlayer(player, world);
        } else {
            state = MobState.IDLE;
            if (path == null || path.isEmpty()) {
                wander(world);
            }
        }
    }

    private void maybeReplanPathToPlayer(Player player, World world) {
        long now = System.currentTimeMillis();
        if (now - lastPathComputeTime < PATH_REPLAN_MS) return;
        lastPathComputeTime = now;

        int tileSize = World.CHUNK_SIZE;

        int mobAbsX = this.chunkX * tileSize + this.x;
        int mobAbsY = this.chunkY * tileSize + this.y;

        int[] playerPos = player.getPlayerLocation();
        int playerAbsX = playerPos[2] * tileSize + playerPos[0];
        int playerAbsY = playerPos[3] * tileSize + playerPos[1];

        List<int[]> newPath = Pathfinder.findPath(world, mobAbsX, mobAbsY, playerAbsX, playerAbsY);
        if (!newPath.isEmpty()) {
            if (!path.isEmpty()) path.clear();
            path = new LinkedList<>(newPath);
            int[] head = path.peek();
            if (head != null && head[0] == mobAbsX && head[1] == mobAbsY) {
                path.poll();
            }
        }
    }

    private int distanceToPlayer(Player player) {
        int mobWorldX = this.chunkX * World.CHUNK_SIZE + this.x;
        int mobWorldY = this.chunkY * World.CHUNK_SIZE + this.y;

        int[] playerPos = player.getPlayerLocation();
        int playerWorldX = playerPos[2] * World.CHUNK_SIZE + playerPos[0];
        int playerWorldY = playerPos[3] * World.CHUNK_SIZE + playerPos[1];

        int dx = playerWorldX - mobWorldX;
        int dy = playerWorldY - mobWorldY;

        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    public int getVisionRadius() { return this.visionDistance; }
    public float getIntelligence() { return this.intelligence; }
    public boolean isPeaceful() { return this.peaceful; }

    @Override
    public void onUse(Player player, Items item) {}
    @Override
    public void onHit(Player player, Items item) {
        this.removeHealth(item.getDamage());
    }
}