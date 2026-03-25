package io.github.Project.game.factory;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.EarthStation;
import io.github.Project.game.entities.Ground;
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
    private static final float ROCKET_WIDTH          = 48f;
    private static final float ROCKET_HEIGHT         = 96f;
    private static final float DEBRIS_SIZE           = 50f;
    private static final float SPACE_STATION_WIDTH   = 300f;
    private static final float SPACE_STATION_HEIGHT  = 163f;
    private static final float EARTH_STATION_WIDTH   = 300f;
    private static final float EARTH_STATION_HEIGHT  = 130f;
    private static final float SATELLITE_WIDTH       = 110f;
    private static final float SATELLITE_HEIGHT      = 70f;
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

    // ── Satellite spawning ───────────────────────────────────────────────

    /**
     * Creates a satellite at a random valid position within the given zone,
     * ensuring minimum distance from the station and from existing satellites.
     * The satellite is assigned a random velocity.
     */
    public Satellite spawnSatellite(
            float stationCX, float stationCY,
            com.badlogic.gdx.utils.Array<Satellite> existing,
            float minStationDist, float minSpacing,
            float xMin, float xMax, float yMin, float yMax) {

        float minStDistSq  = minStationDist * minStationDist;
        float minSpacingSq = minSpacing * minSpacing;
        float x, y;
        int attempts = 0;
        do {
            x = com.badlogic.gdx.math.MathUtils.random(xMin, xMax);
            y = com.badlogic.gdx.math.MathUtils.random(yMin, yMax);
            float dsx = x - stationCX, dsy = y - stationCY;
            if (dsx * dsx + dsy * dsy < minStDistSq) continue;
            boolean tooClose = false;
            for (int i = 0; i < existing.size; i++) {
                Satellite ex = existing.get(i);
                float dex = x - (ex.getPosX() + ex.getWidth()  / 2f);
                float dey = y - (ex.getPosY() + ex.getHeight() / 2f);
                if (dex * dex + dey * dey < minSpacingSq) { tooClose = true; break; }
            }
            if (!tooClose) break;
        } while (++attempts < 32);

        Satellite sat = createSatellite(x, y);
        float speed = com.badlogic.gdx.math.MathUtils.random(8f, 22f);
        float angle = com.badlogic.gdx.math.MathUtils.random(0f, 360f);
        sat.setVx(com.badlogic.gdx.math.MathUtils.cosDeg(angle) * speed);
        sat.setVy(com.badlogic.gdx.math.MathUtils.sinDeg(angle) * speed);
        return sat;
    }

}
