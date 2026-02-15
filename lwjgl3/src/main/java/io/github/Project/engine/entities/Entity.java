package io.github.Project.engine.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Entity {
    protected float posX;
    protected float posY;
    protected float vx;
    protected float vy;
    // ADDED: Speed is needed for the strategy to calculate velocity
    protected float speed; 
    
    
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
    
    /**
     * Renders this entity using shared renderers.
     * @param batch Shared SpriteBatch (for textures)
     * @param shapeRenderer Shared ShapeRenderer (for shapes)
     */
    public abstract void render(SpriteBatch batch, ShapeRenderer shapeRenderer);
    public void update(float deltaTime) {
		// Default movement logic: update position based on velocity
		this.posX += this.vx * deltaTime;
		this.posY += this.vy * deltaTime;
	}
    public abstract float getWidth();
    public abstract float getHeight();
}
