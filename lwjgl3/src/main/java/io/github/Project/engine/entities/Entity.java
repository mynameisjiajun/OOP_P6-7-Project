package io.github.Project.engine.entities;

import io.github.Project.engine.interfaces.IMoveable;
import io.github.Project.engine.interfaces.IMovementStrategy;

/**
 * Abstract base class for all game entities.
 * Provides core functionality for position, movement, and rendering.
 */
public abstract class Entity {
    protected float posX;
    protected float posY;
    protected float vx;
    protected float vy;
    protected IMovementStrategy movementStrategy;
    
    /**
     * Creates a new entity at the specified position.
     * @param posX Initial X position
     * @param posY Initial Y position
     */
    public Entity(float posX, float posY) {
        this.posX = posX;
        this.posY = posY;
        this.vx = 0;
        this.vy = 0;
    }
    
    // Getters and setters
    public float getPosX() {
        return posX;
    }
    
    public void setPosX(float posX) {
        this.posX = posX;
    }
    
    public float getPosY() {
        return posY;
    }
    
    public void setPosY(float posY) {
        this.posY = posY;
    }
    
    public float getVx() {
        return vx;
    }
    
    public void setVx(float vx) {
        this.vx = vx;
    }
    
    public float getVy() {
        return vy;
    }
    
    public void setVy(float vy) {
        this.vy = vy;
    }
    
    public IMovementStrategy getMovementStrategy() {
        return movementStrategy;
    }
    
    public void setMovementStrategy(IMovementStrategy movementStrategy) {
        this.movementStrategy = movementStrategy;
    }
    
    /**
     * Updates the entity's state.
     * @param deltaTime Time elapsed since last update
     */
    public abstract void update(float deltaTime);
    
    /**
     * Renders the entity.
     */
    public abstract void render();
    
    /**
     * Gets the entity's width for collision detection.
     * @return Width as float
     */
    public abstract float getWidth();
    
    /**
     * Gets the entity's height for collision detection.
     * @return Height as float
     */
    public abstract float getHeight();
}
