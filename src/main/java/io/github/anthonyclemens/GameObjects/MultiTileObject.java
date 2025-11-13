package io.github.anthonyclemens.GameObjects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.util.Log;

import io.github.anthonyclemens.Player.Player;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.states.Game;

public class MultiTileObject extends GameObject{
    private final int tileWidth;
    private final int tileHeight;
    private final List<TileBlock> blocks = new ArrayList<>();
    private List<TileBlock> sortedBlocks = null;

    private static class TileBlock implements Serializable{
        private final int x;
        private final int y;
        private final int height;
        private final int tileIndex;

        public TileBlock(int x, int y, int height, int tileIndex) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.tileIndex = tileIndex;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getHeight() {
            return height;
        }

        public int getTileIndex() {
            return tileIndex;
        }
    }

    public MultiTileObject(String tileSheet, int x, int y, int chunkX, int chunkY, String objName) {
        super(tileSheet, x, y, chunkX, chunkY, objName);
        this.tileWidth = SpriteManager.getSpriteWidth(tileSheet);
        this.tileHeight = SpriteManager.getSpriteHeight(tileSheet);
        this.solid = true;
        this.hitbox = new Rectangle(x, y, this.tileWidth, this.tileHeight);
    }

    public MultiTileObject(String loadedMTO, int x, int y, int chunkX, int chunkY) {
        super("", x,y, chunkX, chunkY, "");
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(loadedMTO);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + loadedMTO);
            }

            // Read the first line for object properties
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new IllegalArgumentException("File is empty: " + loadedMTO);
            }

            String[] objectParts = firstLine.split(",");
            if (objectParts.length != 2) {
                throw new IllegalArgumentException("Invalid first line format: " + firstLine);
            }
            // Parse the object properties
            this.name = objectParts[0].trim();
            this.tileSheet = objectParts[1].trim();

            // Read the remaining lines for blocks
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Invalid line format: " + line);
                }

                int index = Integer.parseInt(parts[0].trim());
                int blockX = Integer.parseInt(parts[1].trim());
                int blockY = Integer.parseInt(parts[2].trim());
                int h = Integer.parseInt(parts[3].trim());

                addBlock(index, blockX, blockY, h);
            }
        } catch (IOException | IllegalArgumentException e) {
            Log.error("Error loading MultiTileObject from " + loadedMTO, e);
        }
        this.tileWidth = SpriteManager.getSpriteWidth(tileSheet);
        this.tileHeight = SpriteManager.getSpriteHeight(tileSheet);
    }

    @Override
    public void render(IsoRenderer r, int lodLevel) {
        if(blocks.isEmpty()) return;
        sortedBlocks.forEach(block ->
            r.drawHeightedTile(
                this.tileSheet,
                block.getTileIndex(),
                getX() + block.getX(),
                getY() + block.getY(),
                chunkX,
                chunkY,
                block.getHeight()
            )
        );
        if (Game.showDebug&&this.hitbox!=null&&r.getZoom()>=0.8f) {
            r.getGraphics().setColor(Color.black);
            r.getGraphics().draw(hitbox);
        }
    }

    public void addBlock(int index, int x, int y, int h){
        blocks.add(new TileBlock(x, y, h, index));
        sortBlocks();
    }

    @Override
    public void update(IsoRenderer r, int deltaTime) {}

    @Override
    public void calculateHitbox(IsoRenderer r){
        if (blocks.isEmpty() || blocks == null) return;
        if (this.hitbox==null) this.hitbox = new Rectangle(0, 0, 0, 0);

        float minScreenX = Float.MAX_VALUE;
        float minScreenY = Float.MAX_VALUE;
        float maxScreenX = Float.MIN_VALUE;
        float maxScreenY = Float.MIN_VALUE;
        float zoom = r.getZoom();

        for (TileBlock block : blocks) {
            int worldX = getX() + block.getX();
            int worldY = getY() + block.getY();
            int elevation = block.getHeight();

            float screenX = r.calculateIsoX(worldX, worldY, chunkX, chunkY);
            float screenY = r.calculateIsoY(worldX, worldY, chunkX, chunkY) - elevation * (tileHeight / 2f) * zoom;

            float tileW = tileWidth * zoom;
            float tileH = tileHeight * zoom;

            minScreenX = Math.min(minScreenX, screenX);
            minScreenY = Math.min(minScreenY, screenY);
            maxScreenX = Math.max(maxScreenX, screenX + tileW);
            maxScreenY = Math.max(maxScreenY, screenY + tileH);
        }

        hitbox.setBounds(minScreenX, minScreenY, maxScreenX - minScreenX, maxScreenY - minScreenY);
    }

    private void sortBlocks() {
        this.sortedBlocks = blocks.stream()
            .sorted(Comparator.comparingInt(TileBlock::getY)
                            .thenComparingInt(TileBlock::getX))
            .toList();
    }


    @Override
    public void onUse(Player player, Items item) {}

    @Override
    public void onHit(Player player, Items item) {}
}
