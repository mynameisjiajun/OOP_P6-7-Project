package io.github.Project.game.collisionstrategies;

import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.Rocket;

/**
 * PATTERN: Strategy
 * 
 * Handles collision responses specifically for the Rocket entity.
 * Implements different behaviors for:
 * - Ground landing (refuel/heal vs explosion)
 * - Debris/asteroid damage
 * - Station/satellite collisions (no damage to player)
 * 
 * This separates collision logic from the Rocket class itself,
 * following Single Responsibility Principle.
 */
public class RocketCollisionStrategy implements ICollisionStrategy {
    
    // ── Landing validation constants ────────────────────────────────────────
    private static final float MAX_SAFE_LANDING_SPEED = 100f;  // Reduced for stricter landing
    private static final float MIN_UPRIGHT_ANGLE = 80f;        // Narrowed angle range
    private static final float MAX_UPRIGHT_ANGLE = 100f;       // Narrowed angle range
    private static final float LANDING_HEAL_AMOUNT = 50f;
    private static final float LANDING_REFUEL_AMOUNT = 100f;
    private static final float TAKEOFF_GRACE_PERIOD = 4.0f;    // Grace period to allow rocket to lift off (4 seconds)
    private static final float SAFE_TAKEOFF_ALTITUDE = 200f;   // Altitude at which grace period is no longer needed
    
    private final Rocket rocket;
    private final DamageCalculator damageCalculator;
    
    // Callbacks for game events (scene needs to know about these)
    private LandingCallback landingCallback;
    private CollisionCallback collisionCallback;
    
    // Grace period timer to prevent immediate ground collision during initial takeoff
    private float gracePeriodTimer = TAKEOFF_GRACE_PERIOD;
    
    // Track if rocket has successfully taken off (reached safe altitude)
    private boolean hasLiftedOff = false;
    
    public RocketCollisionStrategy(Rocket rocket, DamageCalculator damageCalculator) {
        this.rocket = rocket;
        this.damageCalculator = damageCalculator;
    }
    
    /**
     * Updates the grace period timer and tracks if rocket has lifted off.
     * Once the rocket reaches SAFE_TAKEOFF_ALTITUDE, the grace period is permanently disabled.
     * Must be called each frame from PlayScene.
     */
    public void update(float deltaTime) {
        // Check if rocket has successfully lifted off
        if (!hasLiftedOff && rocket.getPosY() > SAFE_TAKEOFF_ALTITUDE) {
            hasLiftedOff = true;
            gracePeriodTimer = 0; // Immediately disable grace period once safe altitude is reached
        }
        
        // Only count down grace period if rocket hasn't lifted off yet
        if (!hasLiftedOff && gracePeriodTimer > 0) {
            gracePeriodTimer -= deltaTime;
        }
    }
    
    @Override
    public void handleCollision(CollidableEntity self, CollidableEntity other) {
        String otherTag = other.getCollisionTag();
        
        switch (otherTag) {
            case "Ground":
                // Skip ground collision during grace period (takeoff phase)
                if (gracePeriodTimer <= 0) {
                    handleGroundCollision();
                }
                break;
            
            case "Debris":
            case "Asteroid":
                handleDamageCollision(otherTag);
                break;
            
            case "SpaceStation":
            case "Satellite":
                // No damage - rocket can safely touch stations
                break;
            
            default:
                // Unknown collision type - do nothing
                break;
        }
    }
    
    /**
     * Handles rocket landing on ground.
     * 
     * LANDING RULES:
     * - Safe landing: slow speed (<100 px/s) + upright angle (80-100°)
     *   → Refuels (100) and heals (50) rocket
     * - Unsafe landing: too fast OR wrong angle
     *   → Rocket explodes (999 damage = instant death)
     * 
     * GAME MECHANIC: "When rocket returns to Earth and the rocket's butt 
     * touches the ground with proper conditions, it should refuel and restore 
     * health. If landing is improper (too fast or wrong angle), rocket explodes."
     */
    private void handleGroundCollision() {
        float landingSpeed = Math.abs(rocket.getVy());
        float rocketAngle = rocket.getRotation();
        
        // Check if landing is safe
        boolean speedIsSafe = landingSpeed <= MAX_SAFE_LANDING_SPEED;
        boolean angleIsUpright = (rocketAngle >= MIN_UPRIGHT_ANGLE && 
                                   rocketAngle <= MAX_UPRIGHT_ANGLE);
        
        if (speedIsSafe && angleIsUpright) {
            // ✅ SAFE LANDING - Refuel and heal
            rocket.heal(LANDING_HEAL_AMOUNT);
            rocket.refuel(LANDING_REFUEL_AMOUNT);
            
            // Notify scene about successful landing
            if (landingCallback != null) {
                landingCallback.onSafeLanding();
            }
            
        } else {
            // ❌ CRASH LANDING - Explode
            float crashDamage = damageCalculator.calculateDamage("Ground");
            rocket.takeDamage(crashDamage);
            
            // Notify scene about crash
            if (landingCallback != null) {
                landingCallback.onCrashLanding(landingSpeed, rocketAngle);
            }
        }
    }
    
    /**
     * Handles damage from debris or asteroid collisions.
     * 
     * GAME MECHANIC: "Rocket loses health when hit by space debris"
     */
    private void handleDamageCollision(String collisionTag) {
        float damage = damageCalculator.calculateDamage(collisionTag);
        rocket.takeDamage(damage);
        
        // Notify scene for visual/audio effects
        if (collisionCallback != null) {
            collisionCallback.onDamageCollision(collisionTag, damage);
        }
    }
    
    // ── Callback setters ─────────────────────────────────────────────────────
    
    public void setLandingCallback(LandingCallback callback) {
        this.landingCallback = callback;
    }
    
    public void setCollisionCallback(CollisionCallback callback) {
        this.collisionCallback = callback;
    }
    
    /**
     * Resets the grace period and liftoff state.
     * Call this when restarting the game.
     */
    public void resetGracePeriod() {
        gracePeriodTimer = TAKEOFF_GRACE_PERIOD;
        hasLiftedOff = false;
    }
    
    // ── Callback interfaces ──────────────────────────────────────────────────
    
    /**
     * Callback interface for landing events.
     * Scene implements this to handle game-level responses (win condition, effects).
     */
    public interface LandingCallback {
        void onSafeLanding();
        void onCrashLanding(float speed, float angle);
    }
    
    /**
     * Callback interface for collision events.
     * Scene implements this for visual/audio effects.
     */
    public interface CollisionCallback {
        void onDamageCollision(String collisionTag, float damageAmount);
    }
}