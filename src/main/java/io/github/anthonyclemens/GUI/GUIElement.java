package io.github.anthonyclemens.GUI;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;

import io.github.anthonyclemens.Math.TwoDimensionMath;

public abstract class GUIElement {
    protected Rectangle r;

    protected GUIElement(float x, float y, float w, float h) {

        this.r=new Rectangle(x, y, w, h);
    }

    // Abstract method for rendering
    public abstract void render(Graphics g);

    // Abstract method for update
    public abstract void update(Input i);

    // Getters and setters for x and y
    public float getX() {
        return this.r.getX();
    }

    public void setX(float x) {
        this.r.setX(x);
    }

    public float getY() {
        return this.r.getY();
    }

    public void setY(float y) {
        this.r.setY(y);
    }

    public void move(float x, float y){
        this.r.setLocation(x, y);
    }

    public void setWidth(float w){
        this.r.setWidth(w);
    }

    public void setHeight(float h){
        this.r.setHeight(h);
    }

    public float getWidth(){
        return this.r.getWidth();
    }

    public float getHeight(){
        return this.r.getHeight();
    }

    public Rectangle getRect(){
        return this.r;
    }

    public void centerX(GameContainer gc, int y){
        this.move(TwoDimensionMath.getMiddleX(this.r, gc.getWidth()),y);
    }

    @Override
    public String toString(){
        return "GUI Element @ "+this.getX()+", "+this.getY()+" of size "+this.getWidth()+"W, "+this.getHeight()+"H";
    }
}
