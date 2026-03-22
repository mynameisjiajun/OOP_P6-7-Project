package io.github.Project.game.movementstrategy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Ground;

/**
 * Simulates rocket physics: thrust, Earth gravity, atmospheric drag.
 * Uses LibGDX MathUtils for float-native trig (no casts needed).
 */
public class RocketMovementStrategy implements IMovementStrategy {

    private static final float THRUST_POWER     = 500f;
    private static final float ROTATION_SPEED   = 100f;
    private static final float BASE_GRAVITY     = 300f;
    private static final float SPACE_THRESHOLD  = 3000f;
    private static final float EARTH_DRAG       = 0.995f;
    private static final float SPACE_DRAG       = 0.999f;
    
    // Track if we've already triggered ground collision to avoid multiple triggers
    private boolean groundCollisionTriggered = false;

    @Override
    public void updateVelocity(Entity entity) {
        Rocket rocket = (Rocket) entity;
        InputMovement input = rocket.getInput();
        float dt = Gdx.graphics.getDeltaTime();

        // ── Rotation ─────────────────────────────────────────────────────────
        if (input.keyLeft)  rocket.setRotation(rocket.getRotation() + ROTATION_SPEED * dt);
        if (input.keyRight) rocket.setRotation(rocket.getRotation() - ROTATION_SPEED * dt);

        // ── Earth gravity & atmospheric drag (fade out above SPACE_THRESHOLD) ─
        float gravity  = 0f;
        float drag     = SPACE_DRAG;
        if (rocket.getPosY() < SPACE_THRESHOLD) {
            float t = 1f - (rocket.getPosY() / SPACE_THRESHOLD);
            gravity = BASE_GRAVITY * t;
            drag    = MathUtils.lerp(SPACE_DRAG, EARTH_DRAG, t);
        }
        rocket.setVy(rocket.getVy() - gravity * dt);

        // ── Drag ─────────────────────────────────────────────────────────────
        rocket.setVx(rocket.getVx() * drag);
        rocket.setVy(rocket.getVy() * drag);

        // ── Thrust ───────────────────────────────────────────────────────────
        if (input.keyUp) {
            rocket.setVx(rocket.getVx() + MathUtils.cosDeg(rocket.getRotation()) * THRUST_POWER * dt);
            rocket.setVy(rocket.getVy() + MathUtils.sinDeg(rocket.getRotation()) * THRUST_POWER * dt);
        }

        // ── Ground collision detection ─────────────────────────────────────
        // When rocket reaches ground level (y <= 0), trigger ground collision
        if (rocket.getPosY() <= 0) {
            // Only trigger collision once per landing
            if (!groundCollisionTriggered && rocket.getCollisionStrategy() != null) {
                groundCollisionTriggered = true;
                
                // Create a dummy ground entity for the collision handler
                Ground dummyGround = new Ground(0, -50, 400, 100);
                rocket.getCollisionStrategy().handleCollision(rocket, dummyGround);
            }
            
            // Apply ground clamp
            rocket.setPosY(0);
            if (rocket.getVy() < 0) rocket.setVy(0);
            rocket.setVx(rocket.getVx() * 0.85f); // landing friction
        } else {
            // Reset collision trigger when rocket leaves ground
            if (groundCollisionTriggered && rocket.getPosY() > 10f) {
                groundCollisionTriggered = false;
            }
        }
    }
    
    /**
     * Resets the ground collision trigger. 
     * Called when game restarts.
     */
    public void resetGroundCollisionTrigger() {
        groundCollisionTriggered = false;
    }
}