package io.github.Project.game.movementstrategy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Moon;

/**
 * Simulates rocket physics: thrust, Earth gravity, atmospheric drag,
 * and radial Moon gravity.
 *
 * CHANGES — replaced java.lang.Math with LibGDX equivalents:
 *
 * 1. Math.toRadians() + Math.cos() + Math.sin() (double, require (float) cast)
 *    → MathUtils.cosDeg() + MathUtils.sinDeg() (accept degrees directly,
 *      float-native, no intermediate conversion or cast needed).
 *
 * 2. Manual Pythagorean expansion (Math.sqrt(dx*dx + dy*dy)) for moon distance
 *    → Vector2.len(dx, dy) — LibGDX's static float utility for 2D vector length.
 */
public class RocketMovementStrategy implements IMovementStrategy {

    // ── Earth physics ────────────────────────────────────────────────────────
    private float thrustPower    = 500f;
    private float rotationSpeed  = 180f;
    private float baseGravity    = 300f;
    private float spaceThreshold = 3000f;

    // ── Moon physics ─────────────────────────────────────────────────────────
    private final Moon moon;
    private float moonBaseGravity  = 80f;    // Much weaker than Earth's 300f
    private float moonGravityRadius = 1200f; // Pull radius in world units

    // ── Drag coefficients ────────────────────────────────────────────────────
    private float earthDrag = 0.995f;
    private float spaceDrag = 0.999f;

    /** Constructor used during gameplay — moon gravity is active. */
    public RocketMovementStrategy(Moon moon) {
        this.moon = moon;
    }

    /** No-moon constructor for testing or moon-free scenes. */
    public RocketMovementStrategy() {
        this.moon = null;
    }

    @Override
    public void updateVelocity(Entity entity) {
        Rocket rocket    = (Rocket) entity;
        InputMovement input = rocket.getInput();
        float deltaTime  = Gdx.graphics.getDeltaTime();

        // 1. ROTATION ─────────────────────────────────────────────────────────
        if (input.keyLeft)  rocket.setRotation(rocket.getRotation() + rotationSpeed * deltaTime);
        if (input.keyRight) rocket.setRotation(rocket.getRotation() - rotationSpeed * deltaTime);

        // 2. EARTH GRAVITY & DRAG ─────────────────────────────────────────────
        float currentGravity = 0f;
        float currentDrag    = spaceDrag;

        if (rocket.getPosY() < spaceThreshold) {
            float atmosphereFactor = 1.0f - (rocket.getPosY() / spaceThreshold);
            currentGravity = baseGravity * atmosphereFactor;
            currentDrag    = spaceDrag - ((spaceDrag - earthDrag) * atmosphereFactor);
        }

        rocket.setVy(rocket.getVy() - currentGravity * deltaTime);

        // 3. MOON GRAVITY (radial pull) ────────────────────────────────────────
        if (moon != null) {
            float moonCenterX   = moon.getPosX() + moon.getWidth()  / 2f;
            float moonCenterY   = moon.getPosY() + moon.getHeight() / 2f;
            float rocketCenterX = rocket.getPosX() + rocket.getWidth()  / 2f;
            float rocketCenterY = rocket.getPosY() + rocket.getHeight() / 2f;

            float dx = moonCenterX - rocketCenterX;
            float dy = moonCenterY - rocketCenterY;

            // Vector2.len() is the float-native LibGDX equivalent of Math.sqrt(dx²+dy²)
            float distance = Vector2.len(dx, dy);

            if (distance > 0 && distance < moonGravityRadius) {
                float pullStrength = moonBaseGravity * (1.0f - distance / moonGravityRadius);
                float dirX = dx / distance;
                float dirY = dy / distance;

                rocket.setVx(rocket.getVx() + dirX * pullStrength * deltaTime);
                rocket.setVy(rocket.getVy() + dirY * pullStrength * deltaTime);
            }
        }

        // 4. DRAG (applied equally to X and Y) ────────────────────────────────
        rocket.setVx(rocket.getVx() * currentDrag);
        rocket.setVy(rocket.getVy() * currentDrag);

        // 5. THRUST ───────────────────────────────────────────────────────────
        if (input.keyUp) {
            // MathUtils.cosDeg/sinDeg accept degrees directly — no toRadians() needed
            float thrustX = MathUtils.cosDeg(rocket.getRotation()) * thrustPower * deltaTime;
            float thrustY = MathUtils.sinDeg(rocket.getRotation()) * thrustPower * deltaTime;

            rocket.setVx(rocket.getVx() + thrustX);
            rocket.setVy(rocket.getVy() + thrustY);
        }

        // 6. GROUND (Earth launchpad) ──────────────────────────────────────────
        if (rocket.getPosY() <= 0) {
            rocket.setPosY(0);
            if (rocket.getVy() < 0) rocket.setVy(0);
            rocket.setVx(rocket.getVx() * 0.85f); // friction on landing
        }
    }
}
