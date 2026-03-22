package io.github.Project.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.components.FuelComponent;
import io.github.Project.engine.components.HealthComponent;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.engine.interfaces.ICollisionStrategy;

/**
 * REFACTORED: Now uses Component Pattern for health and fuel management.
 * 
 * PATTERN IMPROVEMENTS:
 * - HealthComponent: Encapsulates health logic (was in PlayScene)
 * - FuelComponent: Encapsulates fuel logic (was in PlayScene)
 * - ICollisionStrategy: Delegates collision handling (was in PlayScene)
 * 
 * OOP BENEFITS:
 * - Encapsulation: Rocket manages its own state
 * - Single Responsibility: Each component has one job
 * - Composition: Flexible design (HAS-A vs IS-A)
 */
public class Rocket extends CollidableEntity {

    // ── Component-based state management ────────────────────────────────────
    private final HealthComponent health;
    private final FuelComponent fuel;
    private ICollisionStrategy collisionStrategy;

    // ── Rendering ────────────────────────────────────────────────────────────
    private Texture staticTexture;
    private static final int FRAME_COUNT = 10;
    private static final float FRAME_DURATION = 1f / 12f;
    private final Texture[] frameTextures = new Texture[FRAME_COUNT];
    private Animation<TextureRegion> thrustAnimation;
    private float animStateTime = 0f;

    // ── Physics and dimensions ───────────────────────────────────────────────
    private final float drawFlame;
    private final float drawH;
    private final float originX;
    private final float originY;
    private final InputMovement input;
    private final float width;
    private final float height;
    private float rotation;

    /**
     * Creates a rocket with specified health and fuel capacities.
     * 
     * @param x starting X position
     * @param y starting Y position
     * @param speed initial speed (usually 0, physics handles movement)
     * @param width rocket width
     * @param height rocket height
     * @param maxHealth maximum health capacity
     * @param maxFuel maximum fuel capacity
     * @param fuelDrainRate fuel consumption per second when thrusting
     * @param input input handler for player controls
     */
    public Rocket(float x, float y, float speed, float width, float height,
                  float maxHealth, float maxFuel, float fuelDrainRate,
                  InputMovement input) {
        super(x, y, speed, width, height);
        
        // Initialize components
        this.health = new HealthComponent(maxHealth);
        this.fuel = new FuelComponent(maxFuel, fuelDrainRate);
        
        this.width = width;
        this.height = height;
        this.input = input;
        this.rotation = 90f;

        // Precompute draw dimensions (from original implementation)
        this.drawFlame = height * (157f / 360f);
        this.drawH = height * (517f / 360f);
        this.originX = width / 2f;
        this.originY = drawFlame + height / 2f;

        this.staticTexture = new Texture("rocket.png");

        // Load thrust animation frames
        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frameTextures[i] = new Texture(
                String.format("rocket_frames/Missile_1_Flying_%03d.png", i));
            frames[i] = new TextureRegion(frameTextures[i]);
        }
        thrustAnimation = new Animation<>(FRAME_DURATION, frames);
        thrustAnimation.setPlayMode(Animation.PlayMode.LOOP);

        this.collisionTag = "Rocket";
    }
    
    /**
     * Convenience constructor with default health/fuel values.
     */
    public Rocket(float x, float y, float speed, float width, float height, InputMovement input) {
        this(x, y, speed, width, height,
             100f,  // Default max health
             100f,  // Default max fuel
             5f,    // Default fuel drain rate
             input);
    }

    /**
     * Updates rocket state including fuel consumption.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // Consume fuel when thrusting
        if (input.keyUp && !fuel.isEmpty()) {
            fuel.consume(deltaTime);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        float visualRot = rotation - 90f;

        if (input.keyUp && !fuel.isEmpty()) {
            // Thrust animation
            animStateTime += Gdx.graphics.getDeltaTime();
            TextureRegion frame = thrustAnimation.getKeyFrame(animStateTime);

            batch.draw(frame,
                getPosX(), getPosY() - drawFlame,
                originX, originY,
                width, drawH,
                1f, 1f,
                visualRot);

        } else {
            // Static texture
            animStateTime = 0f;
            batch.draw(staticTexture,
                getPosX(), getPosY(),
                width / 2f, height / 2f,
                width, height,
                1f, 1f, visualRot,
                0, 0, staticTexture.getWidth(), staticTexture.getHeight(),
                false, false);
        }
    }

    // ── Component accessors (delegate to components) ────────────────────────

    /**
     * Applies damage to the rocket.
     * Delegates to HealthComponent.
     */
    public void takeDamage(float amount) {
        health.takeDamage(amount);
    }

    /**
     * Heals the rocket.
     * Delegates to HealthComponent.
     */
    public void heal(float amount) {
        health.heal(amount);
    }

    /**
     * Refuels the rocket.
     * Delegates to FuelComponent.
     */
    public void refuel(float amount) {
        fuel.refuel(amount);
    }

    /**
     * Checks if rocket is alive (health > 0).
     */
    public boolean isAlive() {
        return health.isAlive();
    }

    /**
     * Checks if rocket has fuel.
     */
    public boolean hasFuel() {
        return !fuel.isEmpty();
    }

    /**
     * Gets health percentage for UI display.
     */
    public float getHealthPercentage() {
        return health.getHealthPercentage();
    }

    /**
     * Gets fuel percentage for UI display.
     */
    public float getFuelPercentage() {
        return fuel.getFuelPercentage();
    }

    // ── Collision strategy ───────────────────────────────────────────────────

    /**
     * Sets the collision strategy for this rocket.
     * Allows strategy to be changed at runtime (Strategy Pattern).
     */
    public void setCollisionStrategy(ICollisionStrategy strategy) {
        this.collisionStrategy = strategy;
    }
    
    /**
     * Gets the collision strategy for this rocket.
     */
    public ICollisionStrategy getCollisionStrategy() {
        return collisionStrategy;
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

    /**
     * Refuels and heals the rocket to full capacity.
     */
    public void refuelAndHeal() {
        this.fuel.fullyRefuel();
        this.health.fullyHeal();
    }

    // ── Getters/Setters ──────────────────────────────────────────────────────

    @Override public float getWidth() { return width; }
    @Override public float getHeight() { return height; }

    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }
    public InputMovement getInput() { return input; }

    public HealthComponent getHealthComponent() { return health; }
    public FuelComponent getFuelComponent() { return fuel; }

    // ── Cleanup ──────────────────────────────────────────────────────────────

    public void dispose() {
        if (staticTexture != null) staticTexture.dispose();
        for (Texture t : frameTextures) if (t != null) t.dispose();
    }
}