package io.github.Project.game.collisionstrategies;

import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.SpaceStation;

/**
 * PATTERN: Strategy
 * 
 * Handles collision responses for SpaceStation entities.
 * 
 * GAME MECHANICS:
 * - Station has HIGHER health than both rocket and satellite (requirement)
 * - Station takes LESS damage per hit than satellites (differential scaling)
 * - When station health reaches 0 → game over (requirement)
 * 
 * The station is the most durable structure in the game, representing
 * the player's ultimate objective: protect the space station from debris.
 */
public class SpaceStationCollisionStrategy implements ICollisionStrategy {
    
    private final SpaceStation station;
    private final DamageCalculator damageCalculator;
    
    // Callback for critical events (scene needs to trigger game over)
    private StationEventCallback eventCallback;
    
    public SpaceStationCollisionStrategy(
            SpaceStation station,
            DamageCalculator damageCalculator) {
        this.station = station;
        this.damageCalculator = damageCalculator;
    }
    
    @Override
    public void handleCollision(CollidableEntity self, CollidableEntity other) {
        String otherTag = other.getCollisionTag();
        
        switch (otherTag) {
            case "Debris":
            case "Asteroid":
                handleDamageCollision(otherTag);
                break;
            
            case "Rocket":
                // Rocket can safely dock with station - no damage
                handleDocking();
                break;
            
            case "Satellite":
                // Satellites are attached to station - no collision
                break;
            
            default:
                // Unknown collision - do nothing
                break;
        }
    }
    
    /**
     * Handles damage from debris or asteroid collisions.
     * 
     * DAMAGE SCALING:
     * - Station takes LESS damage than satellites (more robust structure)
     * - Combined with higher max health, this makes station most durable
     * 
     * GAME OVER CONDITION:
     * - When station health reaches 0 → triggers game over
     * - This is the main fail condition for the game
     */
    private void handleDamageCollision(String collisionTag) {
        // Check if station was alive before damage
        boolean wasAlive = station.isAlive();
        
        // Apply damage (station takes LESS damage than satellites)
        float damage = damageCalculator.calculateDamage(collisionTag);
        station.takeDamage(damage);
        
        // Notify scene about damage (for visual effects)
        if (eventCallback != null) {
            eventCallback.onStationDamaged(damage, station.getHealthPercentage());
        }
        
        // Check if this collision destroyed the station
        if (wasAlive && !station.isAlive()) {
            // GAME OVER CONDITION: "Game ends when Space Station health reaches 0"
            if (eventCallback != null) {
                eventCallback.onStationDestroyed();
            }
        }
    }
    
    /**
     * Handles rocket docking with station.
     * In future, this could trigger repair missions or supply delivery.
     * For now, it's a safe collision (no damage to either).
     */
    private void handleDocking() {
        if (eventCallback != null) {
            eventCallback.onRocketDocked();
        }
    }
    
    // ── Callback setter ──────────────────────────────────────────────────────
    
    public void setEventCallback(StationEventCallback callback) {
        this.eventCallback = callback;
    }
    
    // ── Callback interface ───────────────────────────────────────────────────
    
    /**
     * Callback interface for space station events.
     * Scene implements this to handle game-level responses.
     */
    public interface StationEventCallback {
        /**
         * Called when station takes damage.
         * 
         * @param damageAmount amount of damage applied
         * @param healthPercentage remaining health as percentage (0.0 - 1.0)
         */
        void onStationDamaged(float damageAmount, float healthPercentage);
        
        /**
         * Called when station is destroyed (health reaches 0).
         * Should trigger game over condition.
         */
        void onStationDestroyed();
        
        /**
         * Called when rocket docks with station.
         * Future feature: could trigger repair or resupply missions.
         */
        void onRocketDocked();
    }
}
