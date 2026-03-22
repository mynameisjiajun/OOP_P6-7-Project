package io.github.Project.game.factory;

import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.Fuelbar;
import io.github.Project.game.entities.Ground;
import io.github.Project.game.entities.Moon;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Satellite;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.entities.arrow;
import io.github.Project.game.entities.healthbar;

/**
 * PATTERN: Factory
 *
 * Centralises all game-entity construction behind a single class, so that:
 *   - PlayScene is freed from knowing the exact constructor arguments for
 *     every entity type (low coupling).
 *   - Adding or changing an entity's initialisation only requires editing
 *     one place — this factory — rather than hunting through scene code.
 *   - Default sizes, speeds, and collision tags are set consistently for
 *     every entity of a given type.
 *
 * Usage example (inside PlayScene):
 *   EntityFactory factory = new EntityFactory(gameMaster.getInputMovement());
 *   Rocket rocket   = factory.createRocket(0, 0, moon);
 *   Moon   moon     = factory.createMoon(-100, 5000);
 *   Debris d      = factory.createDebris(x, y);
 */
public class EntityFactory {

    // ── Default sizing constants ─────────────────────────────────────────────
    private static final float ROCKET_WIDTH         = 32f;
    private static final float ROCKET_HEIGHT        = 64f;
    private static final float MOON_SIZE            = 200f;
    private static final float DEBRIS_SIZE        = 50f;
    private static final float SPACE_STATION_WIDTH  = 80f;
    private static final float SPACE_STATION_HEIGHT = 60f;
    private static final float HUD_BAR_WIDTH        = 150f;
    private static final float HUD_BAR_HEIGHT       = 20f;

    private final InputMovement inputMovement;

    /**
     * @param inputMovement shared input handler injected from GameMaster;
     *                      needed by entities that read player input directly.
     */
    public EntityFactory(InputMovement inputMovement) {
        this.inputMovement = inputMovement;
    }

    // ── Entity creators ──────────────────────────────────────────────────────

    /**
     * Creates the player's rocket at the given world position.
     * Speed is set to 0 — RocketMovementStrategy handles all physics.
     */
    public Rocket createRocket(float x, float y) {
        return new Rocket(x, y, 0, ROCKET_WIDTH, ROCKET_HEIGHT, inputMovement);
    }

    /**
     * Creates the Moon at the given world position using the standard size.
     */
    public Moon createMoon(float x, float y) {
        return new Moon(x, y, MOON_SIZE, MOON_SIZE);
    }

    /**
     * Creates a single Debris at the given world position.
     * Texture and rotation speed are randomised inside Debris itself.
     */
    public Debris createDebris(float x, float y) {
        return new Debris(x, y, 0, DEBRIS_SIZE, DEBRIS_SIZE);
    }

    /**
     * Creates a SpaceStation at the given world position.
     */
    public SpaceStation createSpaceStation(float x, float y) {
        return new SpaceStation(x, y, SPACE_STATION_WIDTH, SPACE_STATION_HEIGHT);
    }

    /**
     * Creates the direction arrow indicator that tracks from source to target.
     */
    public arrow createArrow(io.github.Project.engine.entities.Entity source,
                             io.github.Project.engine.entities.Entity target) {
        return new arrow(source, target);
    }

    /**
     * Creates a HUD health bar anchored at (x, y).
     * Uses the standard bar dimensions shared across the game.
     */
    public healthbar createHealthBar(float x, float y) {
        return new healthbar(x, y, HUD_BAR_WIDTH, HUD_BAR_HEIGHT);
    }

    /**
     * Creates a HUD fuel bar anchored at (x, y).
     * Positioned slightly below the health bar by convention.
     */
    public Fuelbar createFuelBar(float x, float y) {
        return new Fuelbar(x, y, HUD_BAR_WIDTH, HUD_BAR_HEIGHT);
    }

	public Satellite createSatellite(float x, float y) {
		// TODO Auto-generated method stub
		return null;
	}

	public Ground createGround(float x, float y, float width) {
		// TODO Auto-generated method stub
		return null;
	}
}