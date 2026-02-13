package io.github.Project.engine.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.interfaces.IMovementStrategy;
//testabc
/**
 * Abstract base class for shape-based objects.
 * Provides common functionality for shapes with color and movement.
 */
public abstract class ShapeObject {
    protected Color color;
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float speed;
    protected ShapeRenderer shapeRenderer;
    
    /**
     * Creates a new shape object.
     * @param x X position
     * @param y Y position
     * @param color Shape color
     */
    public ShapeObject(float x, float y, float width, float height, float speed, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.color = color;
  
    }
    
    // Getters and setters
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
    	this.y = y;
    }
    public float getWidth() {
		return width;
	}
    public void setWidth(float width) {
    	this.width = width;
    }
    
    public float getHeight(float height) {
    	return this.height = height;
    }
    
    public void setHeight(float height) {
		this.height = height;
	}
    
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    /**
     * Renders the shape.
     */
    public abstract void render();
    
    /**
     * Updates the shape's state.
     * @param deltaTime Time elapsed since last update
     */
    public abstract void update(float deltaTime);
}
