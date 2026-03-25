package io.github.Project.game.systems.collisionstrategies;

import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;
import io.github.Project.game.systems.damage.DamageCalculator;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Satellite;
import io.github.Project.game.core.events.GameEventListener;
import io.github.Project.game.core.factory.DebrisFactory;
import io.github.Project.game.core.factory.EntityType;
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

    private static final float SHAKE_DURATION  = 0.25f;
    private static final float SHAKE_AMPLITUDE = 10f;

    private final Satellite satellite;
    private final DamageCalculator damageCalculator;
    private final DebrisFactory debrisFactory;

    private DebrisSpawnCallback debrisSpawnCallback;
    private DestroyedCallback   destroyedCallback;
    private Runnable            onHitSound;
    
    public SatelliteCollisionStrategy(
            Satellite satellite, 
            DamageCalculator damageCalculator,
            DebrisFactory debrisFactory) {
        this.satellite = satellite;
        this.damageCalculator = damageCalculator;
        this.debrisFactory = debrisFactory;
    }

    @Override
    public void handleCollision(CollidableEntity self, CollidableEntity other) {
        String otherTag = other.getCollisionTag();

        switch (otherTag) {
            case "Debris":
            case "Asteroid":
                handleDamageCollision(otherTag, other);
                break;

            case "Rocket":
                // Rocket can safely dock with satellite - no damage to either
                break;

            default:
                break;
        }
    }

    /**
     * Handles damage from debris or asteroid collisions.
     * Marks the debris destroyed, triggers shake, and fires sound callback.
     */
    private void handleDamageCollision(String collisionTag, CollidableEntity other) {
        // Guard: don't process already-destroyed debris
        if (other instanceof Debris && ((Debris) other).isDestroyed()) return;

        boolean wasAlive = satellite.isAlive();

        float damage = damageCalculator.calculateDamage(collisionTag);
        satellite.takeDamage(damage);

        // Shake + destroy the incoming debris + sound
        satellite.triggerShake(SHAKE_DURATION, SHAKE_AMPLITUDE);
        if (other instanceof Debris) ((Debris) other).setState(Debris.DebrisState.DESTROYED);
        if (onHitSound != null) onHitSound.run();

        if (wasAlive && !satellite.isAlive()) {
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
        float centerX = satellite.getPosX() + satellite.getWidth() / 2f;
        float centerY = satellite.getPosY() + satellite.getHeight() / 2f;

        List<Debris> newDebris = debrisFactory.createSatelliteDebris(centerX, centerY);

        if (debrisSpawnCallback != null) {
            debrisSpawnCallback.onDebrisSpawned(newDebris);
        }
        if (destroyedCallback != null) {
            destroyedCallback.onDestroyed();
        }
    }

    // ── Callback setters ─────────────────────────────────────────────────────

    public void setDebrisSpawnCallback(DebrisSpawnCallback callback) {
        this.debrisSpawnCallback = callback;
    }

    public void setDestroyedCallback(DestroyedCallback callback) {
        this.destroyedCallback = callback;
    }

    public void setOnHitSound(Runnable callback) {
        this.onHitSound = callback;
    }

    // ── Callback interfaces ──────────────────────────────────────────────────

    public interface DebrisSpawnCallback {
        void onDebrisSpawned(List<Debris> debris);
    }

    public interface DestroyedCallback {
        void onDestroyed();
    }

    // ── Static factory ───────────────────────────────────────────────────

    /**
     * Creates a fully-wired SatelliteCollisionStrategy and assigns it to the satellite.
     */
    public static SatelliteCollisionStrategy create(
            Satellite satellite,
            io.github.Project.game.core.factory.GameObjectFactory factory,
            GameEventListener listener) {

        SatelliteCollisionStrategy strategy = new SatelliteCollisionStrategy(
            satellite,
            factory.createDamageCalculator(EntityType.SATELLITE),
            factory.getDebrisFactory()
        );
        strategy.setOnHitSound(() -> listener.onCollisionSound());
        strategy.setDebrisSpawnCallback((List<Debris> spawned) ->
            listener.onSatelliteDebrisSpawned(spawned));
        strategy.setDestroyedCallback(() ->
            listener.onSatelliteDestroyed(satellite));
        satellite.setCollisionStrategy(strategy);
        return strategy;
    }
}
