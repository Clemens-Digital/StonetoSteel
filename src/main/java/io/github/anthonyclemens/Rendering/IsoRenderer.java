package io.github.anthonyclemens.Rendering;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.Logic.DayNightCycle;
import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.WorldGen.Chunk;
import io.github.anthonyclemens.WorldGen.World;

/**
 * The IsoRenderer class is responsible for rendering isometric tiles and chunks
 * in a game environment. It handles zoom levels, visible chunk calculations,
 * and rendering logic for different levels of detail (LOD).
 */
public class IsoRenderer {
    private static int renderDistance = 8; // Render distance in chunks
    private static final int TILE_SIZE = 18; // Size of the tile in pixels
    private float zoom; // Zoom level for rendering
    private final SpriteSheet worldTileSheet;
    private int offsetX;
    private int offsetY;
    private final World chunkManager; // Reference to the chunk manager
    private int[] visibleChunks; // Array to store the visible chunks
    private boolean firstFrame = true; // Flag to check if it's the first frame
    private final GameContainer container; // Reference to the game container
    private int spriteCols = 0;
    private int spriteRows = 0;
    private Image[] tileCache = null;
    private boolean cameraMoving = false;
    private boolean isSunUp = true;
    private final FastGraphics fastGraphics = new FastGraphics();

    public IsoRenderer(float zoom, String worldTileSheet, World chunkManager, GameContainer container){
        this.zoom = zoom;
        this.worldTileSheet = SpriteManager.getSpriteSheet(worldTileSheet);
        this.chunkManager = chunkManager;
        this.container = container;
        if(this.worldTileSheet == null) return;
        this.spriteCols = this.worldTileSheet.getHorizontalCount();
        this.spriteRows = this.worldTileSheet.getVerticalCount();
        this.tileCache = new Image[spriteCols * spriteRows];

        // Preload/capture sub-images once
        for (int row = 0; row < spriteRows; row++) {
            for (int col = 0; col < spriteCols; col++) {
                tileCache[row * spriteCols + col] = this.worldTileSheet.getSprite(col, row);
            }
        }
    }

    private Image spriteFor(int tileType) {
        int idx = tileType; // assumes tileType maps 1:1 to sheet index
        if (idx < 0 || idx >= tileCache.length) return null;
        return tileCache[idx];
    }

    private int getLODLevel() {
        if (this.zoom > 1f) return 0; // High zoom: Full detail (1x1)
        if (this.zoom > 0.7f) return 1; // Medium zoom: Quarter Chunks
        return 2; // Low zoom: 8x8 blocks
    }

    public void update(GameContainer container, float zoom, float cameraX, float cameraY, boolean isSunUp) {
        int newOffsetX = (container.getWidth() / 2) - (int) (cameraX * zoom);
        int newOffsetY = (container.getHeight() / 2) - (int) (cameraY * zoom);

        cameraMoving = (newOffsetX != offsetX || newOffsetY != offsetY || this.zoom != zoom);

        this.zoom = zoom;
        this.offsetX = newOffsetX;
        this.offsetY = newOffsetY;
        this.isSunUp = isSunUp;

        if (cameraMoving || firstFrame) {
            this.visibleChunks = getVisibleChunkRange(container);
            firstFrame = false;
        }
    }

    public void render() {
        if (this.visibleChunks == null) return;

        int lodLevel = getLODLevel();

        // First pass: tiles
        worldTileSheet.startUse();
        for (int x = visibleChunks[0]; x <= visibleChunks[2]; x++) {
            for (int y = visibleChunks[1]; y <= visibleChunks[3]; y++) {
                renderChunk(x, y);
            }
        }
        worldTileSheet.endUse();

        // Second pass: chunk objects
        for (int x = visibleChunks[0]; x <= visibleChunks[2]; x++) {
            for (int y = visibleChunks[1]; y <= visibleChunks[3]; y++) {
                Chunk c = chunkManager.getChunk(x, y);
                if (c != null) c.render(this, lodLevel);
            }
        }
    }


    public void updateChunksAroundPlayer(int deltaTime, Player player, DayNightCycle env) {
        if(player.getPlayerLocation()==null) return;
        int playerChunkX = player.getPlayerLocation()[2];
        int playerChunkY = player.getPlayerLocation()[3];

        for (int x = playerChunkX - renderDistance; x <= playerChunkX + renderDistance; x++) {
            for (int y = playerChunkY - renderDistance; y <= playerChunkY + renderDistance; y++) {
                Chunk chunk = chunkManager.getChunk(x, y);
                if (chunk != null) {
                    chunk.update(this, deltaTime, player, env);
                }
            }
        }
    }

    public void calculateHitbox(IsoRenderer renderer, Player player) {
        if(player.getPlayerLocation()==null) return;
        int playerChunkX = player.getPlayerLocation()[2];
        int playerChunkY = player.getPlayerLocation()[3];

        for (int x = playerChunkX - renderDistance; x <= playerChunkX + renderDistance; x++) {
            for (int y = playerChunkY - renderDistance; y <= playerChunkY + renderDistance; y++) {
                Chunk chunk = chunkManager.getChunk(x, y);
                if (chunk != null) {
                    chunk.calculateHitbox(renderer);
                }
            }
        }
    }

    private void renderChunk(int chunkX, int chunkY) {
        int blockSize = switch (getLODLevel()) {
            case 0 -> 1; // LOD 0
            case 1 -> 2; // LOD 1
            case 2 -> 8; // LOD 2
            default -> 1;
        };
        renderChunkWithBlockSize(chunkX, chunkY, blockSize);
    }

    private void renderChunkWithBlockSize(int chunkX, int chunkY, int blockSize) {
        Chunk chunk = chunkManager.getChunk(chunkX, chunkY);
        if (chunk == null) return;

        int lodLevel = switch (blockSize) { case 1 -> 0; case 2 -> 1; default -> 2; };
        int lodSize = chunk.getChunkSize() / blockSize;
        float tileRenderSize = TILE_SIZE * zoom * blockSize;

        int halfTileWidth = TILE_SIZE / 2;
        int quarterTileHeight = TILE_SIZE / 4;
        int preCompX = halfTileWidth + (chunkX - chunkY) * World.CHUNK_SIZE * halfTileWidth;
        int preCompY = quarterTileHeight + (chunkX + chunkY) * World.CHUNK_SIZE * quarterTileHeight;

        for (int blockY = 0; blockY < lodSize; blockY++) {
            for (int blockX = 0; blockX < lodSize; blockX++) {
                int tileType = chunk.getLODTile(lodLevel, blockX, blockY);
                Image img = spriteFor(tileType);
                if (img == null) {
                    Log.warn("Missing sprite for tileType " + tileType + " in chunk (" + chunkX + "," + chunkY + ")");
                    continue;
                }

                float isoX = calculateFastIsoX(blockX * blockSize, blockY * blockSize, halfTileWidth, preCompX);
                float isoY = calculateFastIsoY(blockX * blockSize, blockY * blockSize, quarterTileHeight, preCompY);

                drawScaledIsoImage(img, isoX, isoY, tileRenderSize, tileRenderSize);
            }
        }
    }


    public int[] screenToIsometric(float screenX, float screenY) {
        int halfTileWidth = TILE_SIZE / 2;
        int quarterTileHeight = TILE_SIZE / 4;

        //Remove offset and normalize by zoom
        screenX = (screenX - this.offsetX) / zoom;
        screenY = (screenY - this.offsetY) / zoom;

        //Reverse the isometric transformation
        float isoX = (screenX / halfTileWidth + screenY / quarterTileHeight) / 2;
        float isoY = (screenY / quarterTileHeight - screenX / halfTileWidth) / 2;

        //Adjust for horizontal alignment (offset horizontally by half a tile width)
        isoX -= 0.5f;

        //Convert to integer tile coordinates
        int tileX = Math.round(isoX);
        int tileY = Math.round(isoY);

        return chunkManager.getBlockAndChunk(tileX, tileY);
    }

    private int[] getVisibleChunkRange(GameContainer c) {
        int[] topLeft = screenToIsometric(0, 0);
        int[] topRight = screenToIsometric(c.getWidth() - 1f, 0);
        int[] bottomLeft = screenToIsometric(0, c.getHeight() - 1f);
        int[] bottomRight = screenToIsometric(c.getWidth() - 1f, c.getHeight() - 1f);

        int minChunkX = Math.min(Math.min(topLeft[2], topRight[2]), Math.min(bottomLeft[2], bottomRight[2]));
        int minChunkY = Math.min(Math.min(topLeft[3], topRight[3]), Math.min(bottomLeft[3], bottomRight[3]));
        int maxChunkX = Math.max(Math.max(topLeft[2], topRight[2]), Math.max(bottomLeft[2], bottomRight[2]));
        int maxChunkY = Math.max(Math.max(topLeft[3], topRight[3]), Math.max(bottomLeft[3], bottomRight[3]));

        return new int[]{minChunkX, minChunkY, maxChunkX, maxChunkY};
    }

    public float calculateIsoX(int x, int y, int chunkX, int chunkY) {
        int halfTileWidth = (TILE_SIZE / 2);
        return (((x - y) * halfTileWidth + (chunkX - chunkY) * World.CHUNK_SIZE * halfTileWidth) * this.zoom) + offsetX;
    }

    public float calculateIsoY(int x, int y, int chunkX, int chunkY) {
        int quarterTileHeight = (TILE_SIZE / 4);
        return (((x + y) * quarterTileHeight + (chunkX + chunkY) * World.CHUNK_SIZE * quarterTileHeight) * this.zoom) + offsetY;
    }

    public float calculateFastIsoX(int x, int y, int halfTileWidth, int preCompX) {
        return (((x - y) * halfTileWidth + preCompX) * this.zoom) + this.offsetX;
    }

    public float calculateFastIsoY(int x, int y, int quarterTileHeight, int preCompY) {
        return (((x + y) * quarterTileHeight + preCompY) * this.zoom) +this.offsetY;
    }

    private Image getTile(String tileSheet, int tileType) {
        SpriteSheet sheet = SpriteManager.getSpriteSheet(tileSheet);
        return sheet.getSprite(tileType % sheet.getHorizontalCount(), tileType / sheet.getHorizontalCount());
    }

    private void drawScaledIsoImage(Image tile, float isoX, float isoY, float width, float height) {
        tile.drawEmbedded(isoX, isoY, width, height);
    }

    public void drawScaledTile(String tileSheet, int tileType, int xPos, int yPos, int chunkX, int chunkY){
        this.getTile(tileSheet, tileType).draw(calculateIsoX(xPos, yPos, chunkX, chunkY), calculateIsoY(xPos, yPos, chunkX, chunkY), SpriteManager.getSpriteWidth(tileSheet)*zoom, SpriteManager.getSpriteHeight(tileSheet)*zoom);
    }

    public void drawScaledTile(String tileSheet, int tileType, int xPos, int yPos, int chunkX, int chunkY, Color color){
        this.getTile(tileSheet, tileType).draw(calculateIsoX(xPos, yPos, chunkX, chunkY), calculateIsoY(xPos, yPos, chunkX, chunkY), SpriteManager.getSpriteWidth(tileSheet)*zoom, SpriteManager.getSpriteHeight(tileSheet)*zoom, color);
    }

    public void drawTileIso(String tileSheet, int tileType, float xReal, float yReal){
        this.getTile(tileSheet, tileType).draw(xReal, yReal, SpriteManager.getSpriteWidth(tileSheet)*zoom, SpriteManager.getSpriteHeight(tileSheet)*zoom);
    }

    public void drawTileIso(String tileSheet, int tileType, float xReal, float yReal, Color color){
        this.getTile(tileSheet, tileType).draw(xReal, yReal, SpriteManager.getSpriteWidth(tileSheet)*zoom, SpriteManager.getSpriteHeight(tileSheet)*zoom, color);
    }

    public void drawHeightedTile(String tileSheet, int tileType, int xPos, int yPos, int chunkX, int chunkY, int height){
        this.getTile(tileSheet, tileType).draw(calculateIsoX(xPos, yPos, chunkX, chunkY), calculateIsoY(xPos, yPos, chunkX, chunkY)-SpriteManager.getSpriteWidth(tileSheet)*zoom*height/2f, SpriteManager.getSpriteWidth(tileSheet)*zoom, SpriteManager.getSpriteHeight(tileSheet)*zoom);
    }

    public void drawImageAtCoord(Image i, int xPos, int yPos, int chunkX, int chunkY){
        i.draw(calculateIsoX(xPos, yPos, chunkX, chunkY), calculateIsoY(xPos, yPos, chunkX, chunkY), this.zoom);
    }

    public void drawRectangle(int xPos, int yPos, int chunkX, int chunkY, float width, float height) {
        this.getGraphics().drawRect(calculateIsoX(xPos, yPos, chunkX, chunkY), calculateIsoY(xPos, yPos, chunkX, chunkY), width * zoom, height * zoom);
    }

    public void drawImageAtCoordBatch(Image i, int xPos, int yPos, int chunkX, int chunkY){
        i.drawEmbedded(calculateIsoX(xPos, yPos, chunkX, chunkY), calculateIsoY(xPos, yPos, chunkX, chunkY), this.zoom*i.getWidth(), this.zoom*i.getHeight());
    }

    public void drawImageAtPosition(Image i, int screenX, int screenY){
        i.draw(screenX, screenY, this.zoom);
    }

    public void drawString(String text, String font, int fontSize, float xReal, float yReal, Color color){
        this.getGraphics().setColor(color);
        FontManager.getFont(font, fontSize).drawString(xReal, yReal, text);
    }

    //Getters

    public boolean isCameraMoving() {
        return cameraMoving;
    }

    public Graphics getGraphics(){
        return this.fastGraphics;
    }

    public float getZoom(){
        return this.zoom;
    }
    public static int getRenderDistance() {
        return renderDistance;
    }

    public int getOffsetX(){
        return this.offsetX;
    }

    public int getOffsetY(){
        return this.offsetY;
    }

    public boolean isSunUp(){
        return this.isSunUp;
    }

    //Setters


    public World getChunkManager(){
        return this.chunkManager;
    }

    public static void setRenderDistance(int nrd) {
        IsoRenderer.renderDistance = nrd;
    }
}
