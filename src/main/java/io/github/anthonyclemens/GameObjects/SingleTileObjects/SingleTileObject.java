package io.github.anthonyclemens.GameObjects.SingleTileObjects;

import org.newdawn.slick.Color;
import org.newdawn.slick.geom.Rectangle;

import io.github.anthonyclemens.GameObjects.GameObject;
import io.github.anthonyclemens.Rendering.IsoRenderer;
import io.github.anthonyclemens.Rendering.SpriteManager;
import io.github.anthonyclemens.states.Game;

public class SingleTileObject extends GameObject{
    protected byte i;
    protected int tileWidth;
    protected int tileHeight;

    public SingleTileObject(String tileSheet, String name, int i, int x, int y, int chunkX, int chunkY) {
            super(tileSheet, x, y, chunkX, chunkY, name);
            this.i = (byte) i;
            if(tileSheet != null){
                this.tileWidth = SpriteManager.getSpriteWidth(tileSheet);
                this.tileHeight = SpriteManager.getSpriteHeight(tileSheet);
                this.hitbox.setBounds(0, 0, tileWidth, tileHeight);
            }else{
                this.tileWidth = 0;
                this.tileHeight = 0;
                this.hitbox = new Rectangle(0, 0, 0, 0);
            }
    }

    @Override
    public void render(IsoRenderer r, int lodLevel) {
        r.drawScaledTile(tileSheet,i,x,y,chunkX,chunkY);
        if(Game.showDebug&&this.hitbox!=null&&r.getZoom()>=0.8f){
            r.getGraphics().setColor(Color.black);
            r.getGraphics().draw(hitbox);
        }
    }

    @Override
    public void calculateHitbox(IsoRenderer r) {
        if(this.hitbox==null) this.hitbox = new Rectangle(x,y,this.tileWidth,this.tileHeight);
        if(r.isCameraMoving()||this.alwaysCalcHitbox) hitbox.setBounds(r.calculateIsoX(x, y, chunkX, chunkY), r.calculateIsoY(x, y, chunkX, chunkY), tileWidth*r.getZoom(), tileHeight*r.getZoom());
    }

    @Override
    public void update(IsoRenderer r, int deltaTime) {}

}
