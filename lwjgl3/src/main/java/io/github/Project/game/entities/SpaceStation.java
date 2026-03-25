package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.components.HealthComponent;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;

/**
 * REFACTORED: Now uses Component Pattern for health management.
 * 
 * GAME MECHANIC: "Space station should have higher health than both rocket and satellite"
 * - Default max health: 200 (vs Rocket: 100, Satellite: 50)
 * - Takes LESS damage per hit (via SpaceStationDamageCalculator)
 * - Most durable structure in the game
 * 
 * GAME OVER CONDITION: "Game ends when Space Station health reaches 0"
 */
public class SpaceStation extends CollidableEntity {
    
    protected Texture texture;
    private final float width;
    private final float height;
    
    // Component-based health management
    private final HealthComponent health;
    private ICollisionStrategy collisionStrategy;

    // Shake state — owned by the entity, not the scene
    private float shakeTimer     = 0f;
    private float shakeDuration  = 0f;
    private float shakeAmplitude = 0f;
    
    /**
     * Creates a space station with specified health.
     * 
     * @param x X position
     * @param y Y position
     * @param width width
     * @param height height
     * @param maxHealth maximum health (default: 200)
     */
    public SpaceStation(float x, float y, float width, float height, float maxHealth) {
        super(x, y, 0, width, height);
        this.texture = new Texture("New space assets/Space_Station.png");
        this.width = width;
        this.height = height;
        this.health = new HealthComponent(maxHealth);
        
        this.collisionTag = "SpaceStation";
    }
    
    /**
     * Convenience constructor with default health value.
     * Default: 100 health.
     */
    public SpaceStation(float x, float y, float width, float height) {
        this(x, y, width, height, 100f);
    }
    
    @Override
    public void update(float deltaTime) {
        if (shakeTimer > 0f) shakeTimer = Math.max(0f, shakeTimer - deltaTime);
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (shakeTimer > 0f) {
            float t     = shakeTimer / shakeDuration;
            float amp   = shakeAmplitude * t;
            float phase = shakeTimer * 70f;
            float ox    = MathUtils.sin(phase) * amp;
            float oy    = MathUtils.cos(phase * 0.7f) * amp * 0.45f;
            batch.draw(texture, getPosX() + ox, getPosY() + oy, width, height);
        } else {
            batch.draw(texture, getPosX(), getPosY(), width, height);
        }
    }

    /** Starts a screen-shake + sprite-jitter effect on this entity. */
    public void triggerShake(float duration, float amplitude) {
        this.shakeDuration  = duration;
        this.shakeTimer     = duration;
        this.shakeAmplitude = amplitude;
    }

    public float getShakeTimer()     { return shakeTimer; }
    public float getShakeDuration()  { return shakeDuration; }
    public float getShakeAmplitude() { return shakeAmplitude; }
    
    // ── Component accessors (delegate to HealthComponent) ───────────────────
    
    /**
     * Applies damage to the station.
     * Delegates to HealthComponent.
     */
    public void takeDamage(float amount) {
        health.takeDamage(amount);
    }
    
    /**
     * Heals the station (e.g., repair mission).
     * Delegates to HealthComponent.
     */
    public void heal(float amount) {
        health.heal(amount);
    }

    /** Fully restores station health — called on game restart. */
    public void fullyHeal() {
        health.fullyHeal();
    }
    
    /**
     * Checks if station is still operational (health > 0).
     * 
     * GAME OVER TRIGGER: When this returns false, game should end.
     */
    public boolean isAlive() {
        return health.isAlive();
    }
    
    /**
     * Gets health percentage for UI display.
     * Returns value between 0.0 (destroyed) and 1.0 (full health).
     */
    public float getHealthPercentage() {
        return health.getHealthPercentage();
    }
    
    /**
     * Gets current health value.
     */
    public float getCurrentHealth() {
        return health.getCurrentHealth();
    }
    
    /**
     * Gets maximum health value.
     */
    public float getMaxHealth() {
        return health.getMaxHealth();
    }
    
    // ── Collision strategy ───────────────────────────────────────────────────
    
    /**
     * Sets the collision strategy for this station.
     * Allows strategy to be changed at runtime (Strategy Pattern).
     */
    public void setCollisionStrategy(ICollisionStrategy strategy) {
        this.collisionStrategy = strategy;
    }
    
    /**
     * Handles collision using the assigned strategy.
     * Delegates collision response to strategy (Open/Closed Principle).
     */
    public void onCollision(CollidableEntity other) {
        if (collisionStrategy != null) {
            collisionStrategy.handleCollision(this, other);
        }
    }
    
    // ── Getters ──────────────────────────────────────────────────────────────
    
    @Override
    public float getWidth() {
        return width;
    }
    
    @Override
    public float getHeight() {
        return height;
    }
    
    public HealthComponent getHealthComponent() {
        return health;
    }
    
    // ── Cleanup ──────────────────────────────────────────────────────────────
    
    public void dispose() {
        if (texture != null) texture.dispose();
    }

    public boolean isDestroyed() {
        return !this.health.isAlive();
    }
}