package io.github.Project.engine.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Entity {
    private float posX;
    private float posY;
    private float vx;
    private float vy;
    private float speed; 
    
    
    public Entity(float posX, float posY, float speed) {
        this.posX = posX;
        this.posY = posY;
        this.speed = speed; // Initialize speed
        this.vx = 0;
        this.vy = 0;
    }
    
    // Getters and Setters

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }
    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }
    public float getVx() { return vx; }
    public void setVx(float vx) { this.vx = vx; }
    public float getVy() { return vy; }
    public void setVy(float vy) { this.vy = vy; }
    
    /**
     * Renders this entity using shared renderer.
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
