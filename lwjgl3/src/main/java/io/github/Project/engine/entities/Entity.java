package io.github.Project.engine.entities;

import io.github.Project.engine.interfaces.IMovementStrategy;

public abstract class Entity {
    protected float posX;
    protected float posY;
    protected float vx;
    protected float vy;
    
    // ADDED: Speed is needed for the strategy to calculate velocity
    protected float speed; 
    
    protected IMovementStrategy movementStrategy;
    
    public Entity(float posX, float posY, float speed) {
        this.posX = posX;
        this.posY = posY;
        this.speed = speed; // Initialize speed
        this.vx = 0;
        this.vy = 0;
    }
    
    // --- Getters and Setters ---
    // (Existing ones...)

    // ADDED: Speed Accessors
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    // ... Rest of your getters/setters/abstract methods
    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }
    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }
    public float getVx() { return vx; }
    public void setVx(float vx) { this.vx = vx; }
    public float getVy() { return vy; }
    public void setVy(float vy) { this.vy = vy; }
    public IMovementStrategy getMovementStrategy() { return movementStrategy; }
    public void setMovementStrategy(IMovementStrategy movementStrategy) { 
        this.movementStrategy = movementStrategy; 
    }
    
    public abstract void update(float deltaTime);
    public abstract void render();
    public abstract float getWidth();
    public abstract float getHeight();
}
