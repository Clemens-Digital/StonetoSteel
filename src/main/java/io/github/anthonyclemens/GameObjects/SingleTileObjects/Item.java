package io.github.anthonyclemens.GameObjects.SingleTileObjects;

import org.lwjgl.Sys;
import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;

import io.github.anthonyclemens.GameObjects.Items;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.states.Game;

public class Item extends SingleTileObject{

    private int quantity = 1;
    protected float offsetY = 6f;
    protected float hoverSpeed = 0.1f;
    private transient float hoverTime = 0f;
    private final Color shadowColor = new Color(0, 0, 0, 0.6f);
    private static final float SHADOW_OFFSET_Y = 4f;
    private final long birth;

    public Item(String tileSheet, String name, int i, int x, int y, int chunkX, int chunkY) {
        super(tileSheet, name, i, x, y, chunkX, chunkY);
        this.renderX = 0;
        this.renderY = 0;
        this.birth = Sys.getTime();
    }

    public Item(Items item, int x, int y, int chunkX, int chunkY){
        super(item.getSpriteSheet(), item.name(), item.getSpriteIndex(), x, y, chunkX, chunkY);
        this.renderX = 0;
        this.renderY = 0;
        this.birth = Sys.getTime();
    }

    @Override
    public void render(IsoRenderer r, int deltaTime) {
        if(r.getZoom()>=0.8f){
            float zoom = r.getZoom();
            float baseX = r.calculateIsoX(x, y, chunkX, chunkY);
            float baseY = r.calculateIsoY(x, y, chunkX, chunkY);
            float shadowY = baseY + (tileHeight + SHADOW_OFFSET_Y) * zoom;
            float shadowX = baseX + tileWidth * zoom / 2f;
            float bobOffset = offsetY * (float) Math.sin(hoverTime * hoverSpeed * 2 * Math.PI);
            float scaleFactor = 1f + ((bobOffset / offsetY) * 0.3f);
            float shadowRadius = (tileWidth * zoom / 2f) * scaleFactor;

            // Draw shadow centered under the item
            r.getGraphics().setColor(shadowColor);
            r.getGraphics().fillOval(
                shadowX - shadowRadius / 2f,
                shadowY,
                shadowRadius,
                shadowRadius / 2f
            );
            if (Game.showDebug&&this.hitbox!=null) {
                r.getGraphics().setColor(Color.black);
                r.getGraphics().draw(hitbox);
            }
        }
        if(this.hover){
            r.drawTileIso(tileSheet, i, renderX, renderY, new Color(0.7f, 0.7f, 0.7f, 1f));
        }else{
            r.drawTileIso(tileSheet, i, renderX, renderY);
        }
    }

    @Override
    public void update(IsoRenderer r, int deltaTime) {
        // Accumulate hover time
        hoverTime += deltaTime / 1000f;
    }

    @Override
    public void calculateHitbox(IsoRenderer r){
        if (this.hitbox == null) this.hitbox = new Rectangle(x, y, this.tileWidth, this.tileHeight);
        float bobOffset = offsetY * (float) Math.sin(hoverTime * hoverSpeed * 2 * Math.PI) * r.getZoom();

        renderX = r.calculateIsoX(x, y, chunkX, chunkY);
        renderY = r.calculateIsoY(x, y, chunkX, chunkY) + bobOffset;

        hitbox.setBounds(renderX, renderY, tileWidth * r.getZoom(), tileHeight * r.getZoom());
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getBirthDelta(long currentTime){
        return currentTime-this.birth;
    }

}
