package io.github.Project.engine.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Entity {
    private float posX;
    private float posY;
    private float vx;
    private float vy;
    private float speed;

    /**
     * Constructs an entity at the given position with the given base speed.
     *
     * @param posX  initial X position in world units
     * @param posY  initial Y position in world units
     * @param speed base movement speed (used by subclasses or movement strategies)
     */
    protected Entity(float posX, float posY, float speed) {
        this.posX = posX;
        this.posY = posY;
        this.speed = speed;
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
     *
     * @param batch         shared SpriteBatch for drawing textures
     * @param shapeRenderer shared ShapeRenderer for drawing shapes
     */
    public abstract void render(SpriteBatch batch, ShapeRenderer shapeRenderer);

    /**
     * Advances the entity's position by its current velocity each frame.
     * Subclasses may override to add custom update logic.
     *
     * @param deltaTime time elapsed since the last frame, in seconds
     */
    public void update(float deltaTime) {
        this.posX += this.vx * deltaTime;
        this.posY += this.vy * deltaTime;
    }

    /**
     * Returns the width of this entity's visual/hitbox footprint in world units.
     *
     * @return entity width
     */
    public abstract float getWidth();

    /**
     * Returns the height of this entity's visual/hitbox footprint in world units.
     *
     * @return entity height
     */
    public abstract float getHeight();
}
