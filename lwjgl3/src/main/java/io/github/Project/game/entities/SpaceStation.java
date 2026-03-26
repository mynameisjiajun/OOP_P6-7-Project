package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.components.HealthComponent;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;

// space station entity (main objective to protect)
public class SpaceStation extends CollidableEntity {
    
    protected Texture texture;
    private final float width;
    private final float height;
    
    // health component (handles damage and healing)
    private final HealthComponent health;
    private ICollisionStrategy collisionStrategy;

    // shake effect state
    private float shakeTimer     = 0f;
    private float shakeDuration  = 0f;
    private float shakeAmplitude = 0f;
    
    // create station with custom max health
    public SpaceStation(float x, float y, float width, float height, float maxHealth) {
        super(x, y, 0, width, height);
        this.texture = new Texture("images/entities/Space_Station.png");
        this.width = width;
        this.height = height;
        this.health = new HealthComponent(maxHealth);
        
        this.collisionTag = "SpaceStation";
    }
    
    // create station with default health
    public SpaceStation(float x, float y, float width, float height) {
        this(x, y, width, height, 100f);
    }
    
    @Override
    public void update(float deltaTime) {
        // update shake effect timer
        if (shakeTimer > 0f) shakeTimer = Math.max(0f, shakeTimer - deltaTime);
        updateBounds();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // apply shake effect if active
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

    // trigger shake animation effect
    public void triggerShake(float duration, float amplitude) {
        this.shakeDuration  = duration;
        this.shakeTimer     = duration;
        this.shakeAmplitude = amplitude;
    }

    public float getShakeTimer()     { return shakeTimer; }
    public float getShakeDuration()  { return shakeDuration; }
    public float getShakeAmplitude() { return shakeAmplitude; }
    
    // health operations
    
    // apply damage to station
    public void takeDamage(float amount) {
        health.takeDamage(amount);
    }
    
    // heal station
    public void heal(float amount) {
        health.heal(amount);
    }

    // fully restore health
    public void fullyHeal() {
        health.fullyHeal();
    }
    
    // check if station is still alive
    public boolean isAlive() {
        return health.isAlive();
    }
    
    // get health percentage for UI
    public float getHealthPercentage() {
        return health.getHealthPercentage();
    }
    
    // get current health value
    public float getCurrentHealth() {
        return health.getCurrentHealth();
    }
    
    // get maximum health value
    public float getMaxHealth() {
        return health.getMaxHealth();
    }
    
    // collision strategy
    
    // set collision handling strategy
    public void setCollisionStrategy(ICollisionStrategy strategy) {
        this.collisionStrategy = strategy;
    }
    
    // handle collision using strategy
    public void onCollision(CollidableEntity other) {
        if (collisionStrategy != null) {
            collisionStrategy.handleCollision(this, other);
        }
    }
    
    // getters
    
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
    
    // cleanup
    
    public void dispose() {
        if (texture != null) texture.dispose();
    }

    // check if station is destroyed
    public boolean isDestroyed() {
        return !this.health.isAlive();
    }
}