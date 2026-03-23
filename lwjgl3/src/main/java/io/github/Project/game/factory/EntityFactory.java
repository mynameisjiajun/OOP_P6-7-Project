package io.github.Project.game.factory;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.EarthStation;
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
 * Centralises all game-entity construction so that PlayScene stays
 * decoupled from constructor details.  Adding or resizing an entity
 * only requires editing this file.
 */
public class EntityFactory {

    // ── Default sizing constants ─────────────────────────────────────────
    private static final float ROCKET_WIDTH          = 32f;
    private static final float ROCKET_HEIGHT         = 64f;
    private static final float MOON_SIZE             = 200f;
    private static final float DEBRIS_SIZE           = 50f;
    private static final float SPACE_STATION_WIDTH   = 300f;
    private static final float SPACE_STATION_HEIGHT  = 163f;
    private static final float EARTH_STATION_WIDTH   = 150f;
    private static final float EARTH_STATION_HEIGHT  = 75f;
    private static final float SATELLITE_WIDTH       = 60f;
    private static final float SATELLITE_HEIGHT      = 40f;
    private static final float GROUND_HEIGHT         = 100f;
    private static final float HUD_BAR_WIDTH         = 150f;
    private static final float HUD_BAR_HEIGHT        = 20f;

    private final InputMovement inputMovement;

    public EntityFactory(InputMovement inputMovement) {
        this.inputMovement = inputMovement;
    }

    // ── World entities ───────────────────────────────────────────────────

    /** Player rocket at (x, y) — physics handled by RocketMovementStrategy. */
    public Rocket createRocket(float x, float y) {
        return new Rocket(x, y, 0, ROCKET_WIDTH, ROCKET_HEIGHT, inputMovement);
    }

    /** Orbital space station at (x, y). */
    public SpaceStation createSpaceStation(float x, float y) {
        return new SpaceStation(x, y, SPACE_STATION_WIDTH, SPACE_STATION_HEIGHT);
    }

    /** Earth-side refuelling pad centered at (x, y). */
    public EarthStation createEarthStation(float x, float y) {
        return new EarthStation(x, y, EARTH_STATION_WIDTH, EARTH_STATION_HEIGHT);
    }

    /** Satellite (fragile orbital object that spawns debris on destruction). */
    public Satellite createSatellite(float x, float y) {
        return new Satellite(x, y, SATELLITE_WIDTH, SATELLITE_HEIGHT);
    }

    /** Ground collision + visual entity of the given width. */
    public Ground createGround(float x, float y, float width) {
        return new Ground(x, y, width, GROUND_HEIGHT);
    }

    /** Moon background object at (x, y). */
    public Moon createMoon(float x, float y) {
        return new Moon(x, y, MOON_SIZE, MOON_SIZE);
    }

    /** Single debris piece — velocity must be set by the caller or a DebrisFactory. */
    public Debris createDebris(float x, float y) {
        return new Debris(x, y, 0, DEBRIS_SIZE, DEBRIS_SIZE);
    }

    /** Direction arrow from source entity toward target entity. */
    public arrow createArrow(Entity source, Entity target) {
        return new arrow(source, target);
    }

    // ── HUD elements ─────────────────────────────────────────────────────

    /** Standard-sized health bar anchored at (x, y). */
    public healthbar createHealthBar(float x, float y) {
        return new healthbar(x, y, HUD_BAR_WIDTH, HUD_BAR_HEIGHT);
    }

    /** Custom-sized health bar — used for the large centered station bar. */
    public healthbar createHealthBar(float x, float y, float width, float height) {
        return new healthbar(x, y, width, height);
    }

    /** Standard-sized fuel bar anchored at (x, y). */
    public Fuelbar createFuelBar(float x, float y) {
        return new Fuelbar(x, y, HUD_BAR_WIDTH, HUD_BAR_HEIGHT);
    }
}
