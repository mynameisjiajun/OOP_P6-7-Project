package io.github.Project.engine.objects;

import com.badlogic.gdx.graphics.Color;
import io.github.Project.engine.entities.Entity;

/**
 * Abstract base class for shape-based objects.
 * Provides common functionality for shapes with color and movement.
 */
public abstract class ShapeObject extends Entity {
    protected Color color;
    protected float width;
    protected float height;
    
    /**
     * Creates a new shape object.
     * @param x X position
     * @param y Y position
     * @param width Width of the shape
     * @param height Height of the shape
     * @param speed Movement speed
     * @param color Shape color
     */
    public ShapeObject(float x, float y, float width, float height, float speed, Color color) {
        super(x, y, speed);
        this.width = width;
        this.height = height;
        this.color = color;
        // No longer creating own ShapeRenderer - use shared one from GameMaster
    }
    
    // Getters and setters
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    // FIXED: This was a setter disguised as a getter!
    public float getHeight() {
        return this.height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
}