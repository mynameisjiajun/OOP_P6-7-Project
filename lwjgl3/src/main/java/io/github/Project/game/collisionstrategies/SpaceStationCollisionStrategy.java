package io.github.Project.game.collisionstrategies;

import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.events.GameEventListener;

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
    
    private StationEventCallback eventCallback;
    private DebrisHitCallback    debrisHitCallback;

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
                handleDamageCollision(otherTag, other);
                break;

            case "Rocket":
                if (eventCallback != null) eventCallback.onRocketDocked();
                break;

            case "Satellite":
                break;

            default:
                break;
        }
    }

    /**
     * Handles damage from debris/asteroid collisions.
     * Marks the debris destroyed and fires the visual-FX callback.
     */
    private void handleDamageCollision(String collisionTag, CollidableEntity other) {
        // Guard: don't process already-destroyed debris
        if (other instanceof Debris && ((Debris) other).isDestroyed()) return;

        boolean wasAlive = station.isAlive();

        float damage = damageCalculator.calculateDamage(collisionTag);
        station.takeDamage(damage);

        // Fire visual FX at the debris impact point, then destroy it
        if (other instanceof Debris) {
            Debris d = (Debris) other;
            if (debrisHitCallback != null)
                debrisHitCallback.onHit(d.getPosX() + d.getWidth() / 2f,
                                        d.getPosY() + d.getHeight() / 2f);
            d.setDestroyed(true);
        }

        if (eventCallback != null)
            eventCallback.onStationDamaged(damage, station.getHealthPercentage());

        if (wasAlive && !station.isAlive() && eventCallback != null)
            eventCallback.onStationDestroyed();
    }

    // ── Callback setters ─────────────────────────────────────────────────────

    public void setEventCallback(StationEventCallback callback) {
        this.eventCallback = callback;
    }

    public void setDebrisHitCallback(DebrisHitCallback callback) {
        this.debrisHitCallback = callback;
    }

    // ── Callback interfaces ──────────────────────────────────────────────────

    public interface StationEventCallback {
        void onStationDamaged(float damageAmount, float healthPercentage);
        void onStationDestroyed();
        void onRocketDocked();
    }

    @FunctionalInterface
    public interface DebrisHitCallback {
        void onHit(float x, float y);
    }

    // ── Static factory ───────────────────────────────────────────────────

    /**
     * Creates a fully-wired SpaceStationCollisionStrategy and assigns it to the station.
     */
    public static SpaceStationCollisionStrategy create(
            SpaceStation station,
            io.github.Project.game.factory.GameObjectFactory factory,
            GameEventListener listener) {

        DamageCalculator dmg = factory.createDamageCalculator("SpaceStation");
        SpaceStationCollisionStrategy strategy = new SpaceStationCollisionStrategy(station, dmg);

        strategy.setDebrisHitCallback((x, y) -> listener.onDebrisHitFx(x, y));

        strategy.setEventCallback(new StationEventCallback() {
            @Override public void onStationDamaged(float d, float pct) {
                listener.onCollisionSound();
            }
            @Override public void onStationDestroyed() {
                listener.onStationDestroyed();
            }
            @Override public void onRocketDocked() { }
        });

        station.setCollisionStrategy(strategy);
        return strategy;
    }
}
