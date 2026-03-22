package io.github.Project.game.collisionstrategies;

import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Satellite;
import io.github.Project.game.factory.DebrisFactory;
import java.util.List;

/**
 * PATTERN: Strategy
 * 
 * Handles collision responses for Satellite entities.
 * 
 * KEY MECHANIC: When satellite health reaches 0, it spawns additional debris.
 * This implements the educational goal: "Satellite destruction → spawns more debris"
 * demonstrating the cascading problem of space debris.
 * 
 * DAMAGE SCALING: Satellites take MORE damage than space station (requirement).
 */
public class SatelliteCollisionStrategy implements ICollisionStrategy {
    
    private final Satellite Satellite;
    private final DamageCalculator damageCalculator;
    private final DebrisFactory debrisFactory;
    
    // Callback for debris spawning (scene needs to add debris to world)
    private DebrisSpawnCallback debrisSpawnCallback;
    
    public SatelliteCollisionStrategy(
            Satellite satellite, 
            DamageCalculator damageCalculator,
            DebrisFactory debrisFactory) {
        this.Satellite = satellite;
        this.damageCalculator = damageCalculator;
        this.debrisFactory = debrisFactory;
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
                // Rocket can safely dock with satellite - no damage to either
                break;
            
            default:
                // Unknown collision - do nothing
                break;
        }
    }
    
    /**
     * Handles damage from debris or asteroid collisions.
     * 
     * GAME MECHANICS:
     * 1. "Satellites should take more damage than the space station"
     *    → Uses SatelliteDamageCalculator with higher damage values
     * 
     * 2. "When a satellite's health reaches 0, it spawns additional space debris"
     *    → Triggers debris cloud generation on destruction
     */
    private void handleDamageCollision(String collisionTag) {
        // Check if satellite was alive before damage
        boolean wasAlive = Satellite.isAlive();
        
        // Apply damage (satellites take MORE damage than station)
        float damage = damageCalculator.calculateDamage(collisionTag);
        Satellite.takeDamage(damage);
        
        // Check if this collision killed the satellite
        if (wasAlive && !Satellite.isAlive()) {
            spawnDebrisCloud();
        }
    }
    
    /**
     * Spawns debris cloud when satellite is destroyed.
     * 
     * EDUCATIONAL MECHANIC: Each destroyed satellite creates 3-5 new debris pieces,
     * demonstrating how the space debris problem compounds itself.
     * This is a key educational point about the Kessler Syndrome.
     */
    private void spawnDebrisCloud() {
        float centerX = Satellite.getPosX() + Satellite.getWidth() / 2f;
        float centerY = Satellite.getPosY() + Satellite.getHeight() / 2f;
        
        // Create 3-5 debris pieces from destroyed satellite
        List<Debris> newDebris = debrisFactory.createSatelliteDebris(centerX, centerY);
        
        // Notify scene to add debris to game world
        if (debrisSpawnCallback != null) {
            debrisSpawnCallback.onDebrisSpawned(newDebris);
        }
    }
    
    // ── Callback setter ──────────────────────────────────────────────────────
    
    public void setDebrisSpawnCallback(DebrisSpawnCallback callback) {
        this.debrisSpawnCallback = callback;
    }
    
    // ── Callback interface ───────────────────────────────────────────────────
    
    /**
     * Callback interface for debris spawning events.
     * Scene implements this to add newly spawned debris to the game world.
     */
    public interface DebrisSpawnCallback {
        /**
         * Called when satellite is destroyed and debris needs to be added.
         * 
         * @param debris list of newly created debris entities
         */
        void onDebrisSpawned(List<Debris> debris);
    }
}
