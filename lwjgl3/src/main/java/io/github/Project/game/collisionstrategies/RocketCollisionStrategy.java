package io.github.Project.game.collisionstrategies;

import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.interfaces.ICollisionStrategy;
import io.github.Project.game.damage.DamageCalculator;
import io.github.Project.game.entities.Rocket;

/**
 * PATTERN: Strategy
 *
 * Handles all collision responses for the Rocket entity:
 *
 *  "Ground"       — crash check only (no refuel; ground is not a safe pad).
 *  "EarthStation" — crash check + refuel/heal on safe landing (the real pad).
 *  "Debris"       — handled upstream in PlayScene (bowl-attach logic).
 *  "SpaceStation" / "Satellite" — safe docking, no damage.
 */
public class RocketCollisionStrategy implements ICollisionStrategy {

    // ── Landing validation ───────────────────────────────────────────────
    private static final float MAX_SAFE_LANDING_SPEED = 100f;
    private static final float MIN_UPRIGHT_ANGLE      = 80f;
    private static final float MAX_UPRIGHT_ANGLE      = 100f;
    private static final float LANDING_HEAL_AMOUNT    = 50f;
    private static final float LANDING_REFUEL_AMOUNT  = 100f;
    private static final String TAG_GROUND            = "Ground";

    // ── Grace period (prevents immediate ground collision on takeoff) ─────
    private static final float TAKEOFF_GRACE_PERIOD   = 4.0f;
    private static final float SAFE_TAKEOFF_ALTITUDE  = 200f;

    private final Rocket           rocket;
    private final DamageCalculator damageCalculator;

    private LandingCallback  landingCallback;
    private RefuelCallback   refuelCallback;

    private float   gracePeriodTimer = TAKEOFF_GRACE_PERIOD;
    private boolean hasLiftedOff     = false;

    public RocketCollisionStrategy(Rocket rocket, DamageCalculator damageCalculator) {
        this.rocket           = rocket;
        this.damageCalculator = damageCalculator;
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
                if (gracePeriodTimer <= 0) handleGroundCollision();
                break;
            case "EarthStation":
                if (gracePeriodTimer <= 0) handleEarthStationCollision();
                break;
            case "SpaceStation":
            case "Satellite":
                // Safe docking — no damage
                break;
            case "Debris":
                // Bowl-attach logic lives in PlayScene.handleCollision()
                break;
            default:
                break;
        }
    }

    // ── Ground: crash check only — no refuel ────────────────────────────

    private void handleGroundCollision() {
        float landingSpeed = Math.abs(rocket.getVy());
        float angle        = rocket.getRotation();
        boolean safe = landingSpeed <= MAX_SAFE_LANDING_SPEED
                    && angle >= MIN_UPRIGHT_ANGLE
                    && angle <= MAX_UPRIGHT_ANGLE;

        if (safe) {
            if (landingCallback != null) landingCallback.onSafeLanding();
        } else {
            rocket.takeDamage(damageCalculator.calculateDamage(TAG_GROUND));
            if (landingCallback != null)
                landingCallback.onCrashLanding(landingSpeed, angle);
        }
    }

    // ── EarthStation: safe landing = refuel + heal ───────────────────────

    private void handleEarthStationCollision() {
        float landingSpeed = Math.abs(rocket.getVy());
        float angle        = rocket.getRotation();
        boolean safe = landingSpeed <= MAX_SAFE_LANDING_SPEED
                    && angle >= MIN_UPRIGHT_ANGLE
                    && angle <= MAX_UPRIGHT_ANGLE;

        if (safe) {
            rocket.heal(LANDING_HEAL_AMOUNT);
            rocket.refuel(LANDING_REFUEL_AMOUNT);
            if (refuelCallback  != null) refuelCallback.onRefuel();
            if (landingCallback != null) landingCallback.onSafeLanding();
        } else {
            rocket.takeDamage(damageCalculator.calculateDamage(TAG_GROUND));
            if (landingCallback != null)
                landingCallback.onCrashLanding(landingSpeed, angle);
        }
    }

    // ── Callback setters ─────────────────────────────────────────────────

    public void setLandingCallback(LandingCallback cb) { this.landingCallback = cb; }
    public void setRefuelCallback(RefuelCallback cb)   { this.refuelCallback  = cb; }

    /** Resets grace period — call this on game restart. */
    public void resetGracePeriod() {
        gracePeriodTimer = TAKEOFF_GRACE_PERIOD;
        hasLiftedOff     = false;
    }

    // ── Callback interfaces ──────────────────────────────────────────────

    public interface LandingCallback {
        void onSafeLanding();
        void onCrashLanding(float speed, float angle);
    }

    /** Fired when the rocket lands safely on the EarthStation pad. */
    public interface RefuelCallback {
        void onRefuel();
    }
}
