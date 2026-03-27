package io.github.Project.game.systems.collisionstrategies;

import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.core.events.GameEventListener;

/**
 * PATTERN: Strategy
 *
 * Handles all collision responses for the Rocket entity:
 *
 *  "Ground"       — crash check; safe landing does nothing.
 *  "EarthStation" — crash check; safe landing fires onPadLanding (repairs station).
 *  "Debris"       — handled upstream in PlayScene (bowl-attach logic).
 *  "SpaceStation" / "Satellite" — safe docking, no damage.
 */
public class RocketCollisionStrategy implements ICollisionStrategy {

    // Landing guide display thresholds (no longer used for crash logic)
    public static final float MAX_SAFE_LANDING_SPEED = 160f;
    public static final float MIN_UPRIGHT_ANGLE      = 65f;
    public static final float MAX_UPRIGHT_ANGLE      = 115f;
    private static final String TAG_GROUND           = "Ground";
    private static final String TAG_EARTH_STATION    = "EarthStation";
    private static final String TAG_SPACE_STATION    = "SpaceStation";
    private static final String TAG_SATELLITE        = "Satellite";
    private static final String TAG_DEBRIS           = "Debris";

    // Grace period (prevents immediate ground collision on takeoff)
    private static final float TAKEOFF_GRACE_PERIOD  = 4.0f;
    private static final float SAFE_TAKEOFF_ALTITUDE = 200f;

    private final Rocket rocket;

    private LandingCallback      landingCallback;
    private DebrisCaughtCallback debrisCaughtCallback;

    private float   gracePeriodTimer = TAKEOFF_GRACE_PERIOD;
    private boolean hasLiftedOff     = false;

    public RocketCollisionStrategy(Rocket rocket) {
        this.rocket = rocket;
    }

    /** Must be called every frame from PlayScene to tick the grace period. */
    public void update(float deltaTime) {
        if (!hasLiftedOff && rocket.getPosY() > SAFE_TAKEOFF_ALTITUDE) {
            hasLiftedOff     = true;
            gracePeriodTimer = 0f;
        }
        if (!hasLiftedOff && gracePeriodTimer > 0) {
            gracePeriodTimer -= deltaTime;
        }
    }

    @Override
    public void handleCollision(CollidableEntity self, CollidableEntity other) {
        String tag = other.getCollisionTag();
        switch (tag) {
            case TAG_GROUND:
                if (gracePeriodTimer <= 0) handleGroundLanding();
                break;
            case TAG_EARTH_STATION:
                if (gracePeriodTimer <= 0) handlePadLanding();
                break;
            case TAG_SPACE_STATION:
            case TAG_SATELLITE:
                // Safe docking — no damage
                break;
            case TAG_DEBRIS:
                if (debrisCaughtCallback != null && other instanceof Debris)
                    debrisCaughtCallback.onCaught((Debris) other);
                break;
            default:
                break;
        }
    }

    // Ground: always crash (not the safe pad)

    private void handleGroundLanding() {
        if (landingCallback != null)
            landingCallback.onCrashLanding(Math.abs(rocket.getVy()), rocket.getRotation());
    }

    // EarthStation pad: always safe — no crash possible

    private void handlePadLanding() {
        if (landingCallback != null) landingCallback.onPadLanding();
    }

    /** Used by PlayScene to colour-code the landing guide HUD. */
    public boolean isSafeLanding() {
        float speed = Math.abs(rocket.getVy());
        float angle = rocket.getRotation();
        return speed <= MAX_SAFE_LANDING_SPEED
            && angle >= MIN_UPRIGHT_ANGLE
            && angle <= MAX_UPRIGHT_ANGLE;
    }

    // Callback setters

    public void setLandingCallback(LandingCallback cb) { this.landingCallback = cb; }

    public void setDebrisCaughtCallback(DebrisCaughtCallback cb) { this.debrisCaughtCallback = cb; }

    /** Resets grace period — call this on game restart. */
    public void resetGracePeriod() {
        gracePeriodTimer = TAKEOFF_GRACE_PERIOD;
        hasLiftedOff     = false;
    }

    // Callback interfaces

    public interface LandingCallback {
        void onPadLanding();
        void onCrashLanding(float speed, float angle);
    }

    @FunctionalInterface
    public interface DebrisCaughtCallback {
        void onCaught(Debris d);
    }

    // Static factory

    /**
     * Creates a fully-wired RocketCollisionStrategy and assigns it to the rocket.
     * Strategy wiring lives here so CollisionSetup stays thin.
     */
    public static RocketCollisionStrategy create(
            Rocket rocket,
            io.github.Project.game.core.factory.DebrisFactory debrisManager,
            GameEventListener listener) {

        RocketCollisionStrategy strategy = new RocketCollisionStrategy(rocket);

        strategy.setLandingCallback(new LandingCallback() {
            @Override public void onPadLanding() { listener.onPadLanding(); }
            @Override public void onCrashLanding(float speed, float angle) {
                listener.onCrashLanding(speed, angle);
            }
        });

        strategy.setDebrisCaughtCallback(d -> debrisManager.attachDebris(d));
        rocket.setCollisionStrategy(strategy);
        return strategy;
    }
}
